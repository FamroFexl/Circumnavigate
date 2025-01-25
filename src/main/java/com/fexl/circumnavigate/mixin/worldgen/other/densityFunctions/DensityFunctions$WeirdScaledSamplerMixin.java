/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other.densityFunctions;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$WeirdScaledSampler")
public class DensityFunctions$WeirdScaledSamplerMixin {
	@Shadow @Final private DensityFunction input;
	@Shadow @Final private DensityFunction.NoiseHolder noise;
	@Shadow @Final private DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper;

	@Inject(method = "transform", at = @At("HEAD"), cancellable = true)
	public void transform(DensityFunction.FunctionContext context, double value, CallbackInfoReturnable<Double> cir) {
		if(TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			double d = this.rarityValueMapper.mapper.get(value);
			cir.setReturnValue(Math.abs(this.noise.getValue(context.blockX(), (double)context.blockY() / d, context.blockZ())));
		}
	}
}
