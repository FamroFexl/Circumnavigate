/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.client.screens;

import com.fexl.circumnavigate.mixin.worldInit.PrimaryLevelDataMixin;
import com.fexl.circumnavigate.options.WrappingSettings;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

/**
 * Step 2: Display a screen showing {@link com.fexl.circumnavigate.options.WrappingSettings}. On world creation, they are processed by {@link PrimaryLevelDataMixin}
 */
public class WrappingSettingsScreen extends Screen {
	private final Screen lastScreen;

	@Nullable
	private GridLayout bottomButtons;
	private CycleButton button;

	private EditBox xMin;
	private EditBox xMax;
	private EditBox zMin;
	private EditBox zMax;

	private CycleButton axis;

	private EditBox axisShift;

	@Nullable
	private TabNavigationBar tabNavigationBar;

	private final TabManager tabManager = new TabManager(this::addRenderableWidget, guiEventListener -> this.removeWidget((GuiEventListener)guiEventListener));

	public WrappingSettingsScreen(@Nullable Screen lastScreen, CycleButton button) {
		super((Component)Component.literal("World Wrapping Settings"));
		this.lastScreen = lastScreen;
		this.button = button;

	}

	@Override
	protected void init() {
		GridLayout.RowHelper rowHelper = new GridLayout().columnSpacing(10).rowSpacing(4).createRowHelper(2);

		rowHelper.addChild(new StringWidget(Component.literal("Minimum X"), this.font), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft().alignVerticallyMiddle());
		xMin = rowHelper.addChild(new EditBox(WrappingSettingsScreen.this.font, 50, 20, (Component) Component.literal((String) "Minimum X")));
		rowHelper.addChild(new StringWidget(Component.literal("Maximum X"), this.font), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft().alignVerticallyMiddle());
		xMax = rowHelper.addChild(new EditBox(WrappingSettingsScreen.this.font, 50, 20, (Component)Component.literal((String)"Maximum X")));
		rowHelper.addChild(new StringWidget(Component.literal("Minimum Z"), this.font), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft().alignVerticallyMiddle());
		zMin = rowHelper.addChild(new EditBox(WrappingSettingsScreen.this.font, 50, 20, (Component)Component.literal((String)"Minimum Z")));
		rowHelper.addChild(new StringWidget(Component.literal("Maximum Z"), this.font),  new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft().alignVerticallyMiddle());
		zMax = rowHelper.addChild(new EditBox(WrappingSettingsScreen.this.font, 50, 20, (Component)Component.literal((String)"Maximum Z")));
		rowHelper.addChild(SpacerElement.height(28), 2);
		rowHelper.addChild(new StringWidget(Component.literal("Shifted Axis"), this.font), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft().alignVerticallyMiddle());
		axis = rowHelper.addChild(CycleButton.builder(WrappingSettings.Axis::getText).withValues(WrappingSettings.Axis.X, WrappingSettings.Axis.Z).withInitialValue(WrappingSettings.Axis.X).displayOnlyValue().create(0,0,50,20,Component.literal("Shifted Axis")), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyCenter());
		rowHelper.addChild(new StringWidget(Component.literal("Shift Amount"), this.font), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft().alignVerticallyMiddle());
		axisShift = rowHelper.addChild(new EditBox(WrappingSettingsScreen.this.font, 50, 20, (Component)Component.literal((String)"Shift Amount")));

		rowHelper.getGrid().visitWidgets(this::addRenderableWidget);
		rowHelper.getGrid().setPosition(this.width / 2 - 155, 12);
		rowHelper.getGrid().arrangeElements();


		GridLayout.RowHelper bottomButtons = new GridLayout().columnSpacing(10).createRowHelper(2);
		bottomButtons.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.button.setValue(checkAndOperate());
			WrappingSettingsScreen.this.minecraft.setScreen(lastScreen);
		}).build());

		bottomButtons.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> {
			this.button.setValue(false);
			WrappingSettingsScreen.this.minecraft.setScreen(lastScreen);
		}).build());

		bottomButtons.getGrid().visitWidgets(this::addRenderableWidget);
		bottomButtons.getGrid().setPosition(this.width / 2 - 155, this.height - 28);
		bottomButtons.getGrid().arrangeElements();
	}

	private boolean checkAndOperate() {
		int xMin1;
		int xMax1;
		int zMin1;
		int zMax1;
		WrappingSettings.Axis axis1;
		int axisShift1;

		try {
			xMin1 = Integer.parseInt(xMin.getValue());
			xMax1 = Integer.parseInt(xMax.getValue());
			zMin1 = Integer.parseInt(zMin.getValue());
			zMax1 = Integer.parseInt(zMax.getValue());
			axis1 = (WrappingSettings.Axis) axis.getValue();
			axisShift1 = Integer.parseInt(axisShift.getValue() != "" ? axisShift.getValue() : "0");
		}
		//Invalid values
		catch (NumberFormatException exception) {
			return false;
		}

		//Cannot have a min be greater than a max
		if(xMin1 > xMax1 || zMin1 > zMax1) {
			return false;
		}

		//Cannot have an axis less than 8 chunks
		if(Math.abs(xMax1 - xMin1) < 8 || Math.abs(zMax1 - zMin1) < 8) {
			return false;
		}

		new WrappingSettings(xMin1, xMax1, zMin1, zMax1, axis1, axisShift1);
		return true;
	}

	private FormattedCharSequence formatInput(String command, int maxLength) {

		return null;
	}
}
