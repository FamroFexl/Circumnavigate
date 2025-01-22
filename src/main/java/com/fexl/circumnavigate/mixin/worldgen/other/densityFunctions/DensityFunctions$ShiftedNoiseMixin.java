/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other.densityFunctions;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$ShiftedNoise")
public class DensityFunctions$ShiftedNoiseMixin {
	@Shadow @Final private DensityFunction shiftX;
	@Shadow @Final private DensityFunction shiftY;
	@Shadow @Final private DensityFunction shiftZ;
	@Shadow @Final private double xzScale;
	@Shadow @Final private double yScale;
	@Shadow @Final private DensityFunction.NoiseHolder noise;

	public double compute(DensityFunction.FunctionContext context) {
		NoiseScaling scaledNoise = ((NoiseScaling) (Object) noise);
		scaledNoise.setMul(this.xzScale);
		scaledNoise.setXAdd(this.shiftX.compute(context));
		scaledNoise.setZAdd(this.shiftZ.compute(context));

		double d = (double)context.blockX() * this.xzScale + this.shiftX.compute(context);
		double e = (double)context.blockY() * this.yScale + this.shiftY.compute(context);
		double f = (double)context.blockZ() * this.xzScale + this.shiftZ.compute(context);

		//return this.noise.getValue(d, e, f);
		return this.noise.getValue(context.blockX(), context.blockY() * this.yScale + this.shiftY.compute(context), context.blockZ());
	}
}
