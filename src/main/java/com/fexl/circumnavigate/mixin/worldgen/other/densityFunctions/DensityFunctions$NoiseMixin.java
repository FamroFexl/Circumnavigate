/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other.densityFunctions;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$Noise")
public class DensityFunctions$NoiseMixin {
	@Shadow @Final private DensityFunction.NoiseHolder noise;
	@Shadow @Final private double xzScale;
	@Shadow @Final private double yScale;

	public double compute(DensityFunction.FunctionContext context) {
		((NoiseScaling) (Object) noise).setMul(this.xzScale);
		//return this.noise.getValue(context.blockX() * this.xzScale, (double)context.blockY() * this.yScale, context.blockZ() * this.xzScale);
		return this.noise.getValue(context.blockX(), (double)context.blockY() * this.yScale, context.blockZ());
	}
}
