/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other.densityFunctions;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunction$NoiseHolder")
public class DensityFunction$NoiseHolderMixin implements NoiseScaling {
	@Shadow @Final private Holder<NormalNoise.NoiseParameters> noiseData;
	@Shadow @Final private @Nullable NormalNoise noise;

	private long lastTime =0;

	public double getValue(double x, double y, double z) {
		if(this.noise == null) {
			return 0.0;
		}
		else {
			NoiseScaling scaledNoise = ((NoiseScaling) (Object) noise);
			scaledNoise.setXMul(xMul);
			scaledNoise.setZMul(zMul);
			scaledNoise.setXAdd(xAdd);
			scaledNoise.setZAdd(zAdd);

			return this.noise.getValue(x, y, z);
		}
	}

	double xMul = 1;
	double zMul = 1;
	double xAdd = 0;
	double zAdd = 0;

	@Override
	public void setXMul(double xMul) {
		this.xMul = xMul;
	}

	@Override
	public void setZMul(double zMul) {
		this.zMul = zMul;
	}

	@Override
	public void setXAdd(double xAdd) {
		this.xAdd = xAdd;
	}

	@Override
	public void setZAdd(double zAdd) {
		this.zAdd = zAdd;
	}
}
