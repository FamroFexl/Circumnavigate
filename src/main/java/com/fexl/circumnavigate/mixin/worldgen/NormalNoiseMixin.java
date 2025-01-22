/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NormalNoise.class)
public class NormalNoiseMixin implements NoiseScaling {
	@Shadow @Final private double valueFactor;
	@Shadow @Final private PerlinNoise first;
	@Shadow @Final private PerlinNoise second;

	@Shadow @Final private double maxValue;
	private final double xWidth = 256.0;
	private final double zWidth = 256.0;

	private long lastTime = 0;

	private long source;
	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, NormalNoise.NoiseParameters parameters, boolean useLegacyNetherBiome, CallbackInfo ci) {
		source = random.nextLong();
	}

	public double getValue(double x, double y, double z) {

		double multiplier = 1.0181268882175227;

		double d = x * multiplier;
		double e = y * multiplier;
		double f = z * multiplier;

		NoiseScaling scaledFirst = ((NoiseScaling) (Object) this.first);
		NoiseScaling scaledSecond = ((NoiseScaling) (Object) this.second);

		scaledFirst.setXMul(xMul);
		scaledFirst.setZMul(zMul);
		scaledFirst.setXAdd(xAdd);
		scaledFirst.setZAdd(zAdd);
		scaledSecond.setXMul(multiplier * xMul);
		scaledSecond.setZMul(multiplier * zMul);
		scaledSecond.setXAdd(xAdd);
		scaledSecond.setZAdd(zAdd);

		return (this.first.getValue(x, y, z) + this.second.getValue(x, y, z)) * this.valueFactor;
		//return (this.first.getValue(x, y, z) + this.second.getValue(d, e, f)) * this.valueFactor;
	}

	double xMul = 1;
	double zMul = 1;
	double xAdd = 0;
	double zAdd = 0;

	public void setXMul(double xMul) {
		this.xMul = xMul;
	}

	public void setZMul(double zMul) {
		this.zMul = zMul;
	}

	public void setXAdd(double xAdd) {
		this.xAdd = xAdd;
	}

	public void setZAdd(double zAdd) {
		this.zAdd = zAdd;
	}


}
