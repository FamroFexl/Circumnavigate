/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(NeighborUpdater.class)
public interface NeighborUpdatorMixin {
	@ModifyVariable(method = "executeUpdate", at = @At("HEAD"), index = 2, argsOnly = true)
	private static BlockPos wrapBlockPos(BlockPos blockPos, @Local(argsOnly = true) Level level) {
		return level.getTransformer().onlyServerSide().Block.wrapToBounds(blockPos);
	}

	@ModifyVariable(method = "executeShapeUpdate", at = @At("HEAD"), index = 3, argsOnly = true)
	private static BlockPos wrapBlockPos(BlockPos blockPos, @Local(argsOnly = true) LevelAccessor level) {
		if(level instanceof ServerLevel serverLevel) {
			return serverLevel.getTransformer().Block.wrapToBounds(blockPos);
		}
		return blockPos;
	}

}
