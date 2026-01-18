package com.fexl.circumnavigate.mixin.block.blocks.fluid;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
	@ModifyVariable(method = "spread", at = @At("HEAD"), argsOnly = true, index = 2)
	public BlockPos wrapSpread(BlockPos blockPos, @Local(argsOnly = true) Level level) {
		return new BlockPosWrapped(blockPos, level.getTransformer().SSO());
	}
}
