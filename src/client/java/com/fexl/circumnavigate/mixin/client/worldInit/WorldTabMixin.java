/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.client.worldInit;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.client.screens.WrappingSettingsScreen;
import com.fexl.circumnavigate.client.storage.CreateWorldButtons;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public class WorldTabMixin {
	/**
	 * Adds a "Wrap World" button to the WorldTab, which opens up a wrappingSettings screen.
	 */
	@Inject(method = "<init>(Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;)V", at = @At("TAIL"))
	public void gameTab(CreateWorldScreen createWorldScreen, CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
		//TODO Render distance must be limited to prevent chunk glitching. (renderDistance + 2 > Math.min(xWidth/2, zWidth/2) ? TOO_HIGH : JUST_RIGHT)
		CycleButton.Builder<Boolean> wrappingButton = CycleButton.onOffBuilder(false);
		Tooltip tooltip = Tooltip.create(Component.literal("Wrap the world along boundaries."));
		wrappingButton.withTooltip(boolean_ -> tooltip);
		wrappingButton.displayOnlyValue();
		rowHelper.addChild(new StringWidget(Component.literal("Wrap World"), Minecraft.getInstance().font), new LayoutSettings.LayoutSettingsImpl().alignVerticallyMiddle());
		rowHelper.addChild(CreateWorldButtons.wrappingButton = wrappingButton.create(0, 0, 44, 20, (Component) Component.empty(), (cycleButton, state) -> {
			if(cycleButton.getValue()) {
				Minecraft.getInstance().setScreen(new WrappingSettingsScreen(createWorldScreen, cycleButton));
			}
			else {
				new WrappingSettings(WrappingSettings.DISABLED);
			}
		}), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyRight());
	}




}
