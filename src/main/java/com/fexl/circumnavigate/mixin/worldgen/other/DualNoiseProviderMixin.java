/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DualNoiseProvider.class)
public class DualNoiseProviderMixin {
	@Shadow @Final private float slowScale;
	@Shadow @Final private NormalNoise slowNoise;

	@Inject(method = "getSlowNoiseValue", at = @At("HEAD"), cancellable = true)
	public void getSlowNoiseValue(BlockPos pos, CallbackInfoReturnable<Double> cir) {
		if(TransformerRequests.noiseLevel.getTransformer().wrappingSettings.useWrappedWorldGen()) {
			cir.setReturnValue(this.slowNoise.getValue((double)((float)pos.getX()), (double)((float)pos.getY() * this.slowScale), (double)((float)pos.getZ())));
		}
	}

}
