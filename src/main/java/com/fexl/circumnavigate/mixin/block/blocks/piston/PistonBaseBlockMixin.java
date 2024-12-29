/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block.blocks.piston;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {
	@ModifyVariable(method = "triggerEvent", at = @At("HEAD"), index = 3, argsOnly = true)
	public BlockPos modifyBlockPos(BlockPos blockPos, @Local(argsOnly = true) Level level) {
		return new BlockPosWrapped(blockPos, level.getTransformer().onlyServerSide());
	}
}
