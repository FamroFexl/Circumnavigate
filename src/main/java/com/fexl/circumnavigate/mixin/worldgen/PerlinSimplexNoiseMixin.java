/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.storage.TransformerRequests;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Inject(method = "getValue", at= @At("HEAD"), cancellable = true)
	public void getValue(double x, double y, boolean useNoiseOffsets, CallbackInfoReturnable<Double> cir) {
		if(!TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			return;
		}

		double d = 0.0;
		double e = this.highestFreqInputFactor;
		double f = this.highestFreqValueFactor;

		for (SimplexNoise simplexNoise : this.noiseLevels) {
			if (simplexNoise != null) {
				double finalE = e;
				d += simplexNoise.getValue(x, y) * f;
			}

			e /= 2.0;
			f *= 2.0;
		}

		cir.setReturnValue(d);
	}
}
