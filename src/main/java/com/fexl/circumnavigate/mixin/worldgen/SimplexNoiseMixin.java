/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import com.fexl.circumnavigate.storage.TransformerRequests;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(SimplexNoise.class)
public abstract class SimplexNoiseMixin {
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

	@WrapMethod(method = "getValue(DD)D")
	public double getValue(double x, double y, Operation<Double> original) {
		DimensionTransformer transformer = TransformerRequests.noiseLevel.getTransformer();

		if(!transformer.wrappingSettings.useWrappedWorldGen()) {
			return original.call(x, y);
		}

		//double xa = ((x - xAdd) / xMul) / (xWidth);
		//double za = ((y - zAdd) / zMul) / (zWidth);
		double xa = (x + transformer.wrappingSettings.xChunkBoundMin()*16) / (transformer.xWidth*16);
		double za = (y + transformer.wrappingSettings.zChunkBoundMin()*16) / (transformer.zWidth*16);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		return OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
	}

	@WrapMethod(method = "getValue(DDD)D")
	public double getValue(double x, double y, double z, Operation<Double> original) {
		DimensionTransformer transformer = TransformerRequests.noiseLevel.getTransformer();

		if(!transformer.wrappingSettings.useWrappedWorldGen()) {
			return original.call(x, y, z);
		}

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
}
