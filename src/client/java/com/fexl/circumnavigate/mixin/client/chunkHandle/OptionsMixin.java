/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class OptionsMixin {
	@Shadow public abstract OptionInstance<Integer> renderDistance();

	Options thiz = (Options) (Object) this;
	OptionsAccessorMixin accessor = (OptionsAccessorMixin) thiz;
	@Inject(method = "getEffectiveRenderDistance", at = @At("HEAD"), cancellable = true)
	public void getEffectiveRenderDistance(CallbackInfoReturnable<Integer> cir) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();

		int renderDistance = accessor.getServerRenderDistance() > 0 ? Math.min(accessor.getRenderDistance().get(), accessor.getServerRenderDistance()) : accessor.getRenderDistance().get();
		cir.setReturnValue(transformer.limitViewDistance(renderDistance));
	}
}
