/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedStateProvider.class)
public class NoiseBasedStateProviderMixin {
	@Shadow @Final protected NormalNoise noise;

	@Inject(method = "getNoiseValue", at = @At("HEAD"), cancellable = true)
	public void getNoiseValue(BlockPos pos, double delta, CallbackInfoReturnable<Double> cir) {
		if(TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			cir.setReturnValue(this.noise.getValue((double)pos.getX(), (double)pos.getY() * delta, (double)pos.getZ()));
		}
	}
}
