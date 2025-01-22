/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other.densityFunctions;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$ShiftNoise")
public interface DensityFunctions$ShiftNoiseMixin {
	@Shadow DensityFunction.NoiseHolder offsetNoise();

	default double compute(double x, double y, double z) {
		((NoiseScaling) (Object) offsetNoise()).setMul(0.25);
		//return this.offsetNoise().getValue(x * 0.25, y * 0.25, z * 0.25) * 4.0;
		return this.offsetNoise().getValue(x, y * 0.25, z) * 4.0;
	}
}
