/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PerlinNoise.class)
public class PerlinNoiseMixin {

	@Shadow @Final private ImprovedNoise[] noiseLevels;
	@Shadow @Final private int firstOctave;
	@Shadow @Final private DoubleList amplitudes;
	@Shadow @Final private double lowestFreqValueFactor;
	@Shadow @Final private double lowestFreqInputFactor;
	@Shadow @Final private double maxValue;
	
	private final double xWidth = 256.0;
	private final double zWidth = 256.0;

	//PerlinNoise thiz = (PerlinNoise) (Object) this;

	private long source;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, Pair octavesAndAmplitudes, boolean useNewFactory, CallbackInfo ci) {
		source = random.nextLong();
	}

	@Inject(method = "getValue(DDDDDZ)D", at = @At("HEAD"), cancellable = true)
	public void getValue(double x, double y, double z, double yScale, double yMax, boolean useFixedY, CallbackInfoReturnable<Double> cir) {
		if(!TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			return;
		}


		double d = 0.0;
		double e = this.lowestFreqInputFactor;
		double f = this.lowestFreqValueFactor;

		for (int i = 0; i < this.noiseLevels.length; i++) {
			ImprovedNoise improvedNoise = this.noiseLevels[i];
			if (improvedNoise != null) {
				double g = improvedNoise.noise(x, useFixedY ? -improvedNoise.yo : PerlinNoise.wrap(y * e), z, yScale * e, yMax * e);
				d += this.amplitudes.getDouble(i) * g * f;
			}

			e *= 2.0;
			f /= 2.0;
		}

		cir.setReturnValue(d);
	}
}
