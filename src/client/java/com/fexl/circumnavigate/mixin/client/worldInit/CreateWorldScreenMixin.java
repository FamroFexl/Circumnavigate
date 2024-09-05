/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.worldInit;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.client.storage.CreateWorldButtons;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
	/**
	 * Set the world wrapping to disabled if the worldWrapping settings are not activated.
	 */
	@Inject(method = "onCreate()V", at = @At("HEAD"))
	public void onCreate(CallbackInfo ci) {
		if(!CreateWorldButtons.wrappingButton.getValue()) {
			new WrappingSettings(WrappingSettings.DISABLED);
		}
	}
}
