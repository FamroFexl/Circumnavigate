/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block.blocks.piston;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {
	@Final @Shadow private Level level;

	@ModifyVariable(method = "<init>", at = @At("TAIL"), index = 2, argsOnly = true)
	private BlockPos modifyBlockPos(BlockPos blockPos) {
		return new BlockPosWrapped(blockPos, level.getTransformer().onlyServerSide());
	}
}
