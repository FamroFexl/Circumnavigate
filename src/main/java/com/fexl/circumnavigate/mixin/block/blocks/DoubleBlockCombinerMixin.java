/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block.blocks;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DoubleBlockCombiner.class)
public class DoubleBlockCombinerMixin {
	@ModifyVariable(method = "combineWithNeigbour", argsOnly = true, at = @At("HEAD"), index = 6)
	private static BlockPos modifyBlockPos(BlockPos blockPos, @Local(argsOnly = true)LevelAccessor level) {
		if(level instanceof ServerLevel serverLevel) {
			return new BlockPosWrapped(blockPos, serverLevel.getTransformer());
		}
		return blockPos;
	}
}
