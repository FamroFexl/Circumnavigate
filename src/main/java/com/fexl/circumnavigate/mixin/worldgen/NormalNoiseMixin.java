/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NormalNoise.class)
public class NormalNoiseMixin {
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

	@Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
	public void getValue(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
		if(TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			cir.setReturnValue((this.first.getValue(x, y, z) + this.second.getValue(x, y, z)) * this.valueFactor);
		}
	}


}
