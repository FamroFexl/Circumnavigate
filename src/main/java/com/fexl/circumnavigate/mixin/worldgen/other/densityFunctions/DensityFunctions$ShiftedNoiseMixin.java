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

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$ShiftedNoise")
public class DensityFunctions$ShiftedNoiseMixin {
	@Shadow @Final private DensityFunction shiftX;
	@Shadow @Final private DensityFunction shiftY;
	@Shadow @Final private DensityFunction shiftZ;
	@Shadow @Final private double xzScale;
	@Shadow @Final private double yScale;
	@Shadow @Final private DensityFunction.NoiseHolder noise;

	@Inject(method = "compute", at = @At("HEAD"), cancellable = true)
	public void compute(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
		if(TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			cir.setReturnValue(this.noise.getValue(context.blockX(), context.blockY() * this.yScale + this.shiftY.compute(context), context.blockZ()));
		}

	}
}
