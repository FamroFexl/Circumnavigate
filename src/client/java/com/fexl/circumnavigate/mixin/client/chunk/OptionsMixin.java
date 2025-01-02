/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.chunk;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class OptionsMixin {
	@Shadow private int serverRenderDistance;
	@Shadow @Final private OptionInstance<Integer> renderDistance;

	@Inject(method = "getEffectiveRenderDistance", at = @At("HEAD"), cancellable = true)
	public void getEffectiveRenderDistance(CallbackInfoReturnable<Integer> cir) {
		int renderDistance = serverRenderDistance > 0 ? Math.min(this.renderDistance.get(), serverRenderDistance) : this.renderDistance.get();
		cir.setReturnValue(Minecraft.getInstance().level.getTransformer().limitViewDistance(renderDistance));
	}
}
