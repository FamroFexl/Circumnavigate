/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.injected.NoiseScaling;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PerlinSimplexNoise.class)
public class PerlinSimplexNoiseMixin {
	@Shadow @Final private SimplexNoise[] noiseLevels;
	@Shadow @Final private double highestFreqValueFactor;
	@Shadow @Final private double highestFreqInputFactor;

	private final double xWidth = 256.0;
	private final double zWidth = 256.0;

	private long source;
	@Inject(method = "<init>(Lnet/minecraft/util/RandomSource;Lit/unimi/dsi/fastutil/ints/IntSortedSet;)V", at = @At("TAIL"))
	public void init(RandomSource random, IntSortedSet octaves, CallbackInfo ci) {
		source = random.nextLong();
	}
	
	public double getValue(double x, double y, boolean useNoiseOffsets) {
		/**
		double xa = x / (xWidth);
		double za = y / (zWidth);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		double noise1 = OpenSimplex2S.noise2(source, 0, y);
		return (noise4 + noise1)/2.0;**/

		double d = 0.0;
		double e = this.highestFreqInputFactor;
		double f = this.highestFreqValueFactor;

		for (SimplexNoise simplexNoise : this.noiseLevels) {
			if (simplexNoise != null) {
				((NoiseScaling) (Object) simplexNoise).setMul(e);
				d += simplexNoise.getValue(x * e + (useNoiseOffsets ? simplexNoise.xo : 0.0), y * e + (useNoiseOffsets ? simplexNoise.yo : 0.0)) * f;
			}

			e /= 2.0;
			f *= 2.0;
		}

		return d;
	}
}
