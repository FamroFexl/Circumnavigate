/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other.densityFunctions;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$Noise")
public class DensityFunctions$NoiseMixin {
	@Shadow @Final private DensityFunction.NoiseHolder noise;
	@Shadow @Final private double xzScale;
	@Shadow @Final private double yScale;

	@Inject(method = "compute", at = @At("HEAD"), cancellable = true)
	public void compute(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
		if(TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			cir.setReturnValue(this.noise.getValue(context.blockX(), (double)context.blockY() * this.yScale, context.blockZ()));
		}
	}
}
