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

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$WeirdScaledSampler")
public class DensityFunctions$WeirdScaledSamplerMixin {
	@Shadow @Final private DensityFunction input;
	@Shadow @Final private DensityFunction.NoiseHolder noise;
	@Shadow @Final private DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper;

	public double transform(DensityFunction.FunctionContext context, double value) {
		double d = this.rarityValueMapper.mapper.get(value);

		((NoiseScaling) (Object) noise).setMul(1/d);
		//return d * Math.abs(this.noise.getValue((double)context.blockX() / d, (double)context.blockY() / d, (double)context.blockZ() / d));
		return Math.abs(this.noise.getValue(context.blockX(), (double)context.blockY() / d, context.blockZ()));
	}
}
