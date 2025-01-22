/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.injected.NoiseScaling;
import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimplexNoise.class)
public abstract class SimplexNoiseMixin implements NoiseScaling {
	@Final @Shadow public double xo;
	@Final @Shadow public double yo;
	@Final @Shadow public double zo;
	@Final @Shadow private int[] p;
	@Final @Shadow private static double SQRT_3;
	@Final @Shadow private static double F2;
	@Final @Shadow private static double G2;
	@Final @Shadow abstract public int p(int index);
	@Final @Shadow abstract public double getCornerNoise3D(int gradientIndex, double x, double y, double z, double offset);

	private long source;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, CallbackInfo ci) {
		source = random.nextLong();
	}

	public double getValue(double x, double y) {
		DimensionTransformer transformer = TransformerRequests.noiseLevel.getTransformer();
		//double xa = ((x - xAdd) / xMul) / (xWidth);
		//double za = ((y - zAdd) / zMul) / (zWidth);
		double xa = (x + transformer.wrappingSettings.xChunkBoundMin()*16) / (transformer.xWidth*16);
		double za = (y + transformer.wrappingSettings.zChunkBoundMin()*16) / (transformer.zWidth*16);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		return OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
	}

	public double getValue(double x, double y, double z) {
		DimensionTransformer transformer = TransformerRequests.noiseLevel.getTransformer();
		//double xa = ((x - xAdd) / xMul) / (xWidth);
		//double za = ((z - zAdd) / zMul) / (zWidth);
		double xa = (x + transformer.wrappingSettings.xChunkBoundMin()*16) / (transformer.xWidth*16);
		double za = (y + transformer.wrappingSettings.zChunkBoundMin()*16) / (transformer.zWidth*16);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		double noise1 = OpenSimplex2S.noise2(source, 0, y);
		return (noise4 + noise1)/2.0;
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
