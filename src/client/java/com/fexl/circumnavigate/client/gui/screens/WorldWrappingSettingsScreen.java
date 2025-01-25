/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.client.gui.screens;

import com.fexl.circumnavigate.client.gui.components.MutableCycleButton;
import com.fexl.circumnavigate.core.CoordinateConstants;
import com.fexl.circumnavigate.accessors.WorldWrappingSettingsAccessor;
import com.fexl.circumnavigate.options.DimensionWrappingSettings;
import com.fexl.circumnavigate.options.WorldWrappingPresets;
import com.fexl.circumnavigate.options.WorldWrappingSettings;
import com.fexl.circumnavigate.options.WrappingOptions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A screen for entering world wrapping settings during world creation.
 */
public class WorldWrappingSettingsScreen extends Screen {
	protected CreateWorldScreen createWorldScreen;

	protected Tooltip doneButtonDefault = Tooltip.create(Component.literal("Save wrapping settings"));
	protected Tooltip offsetInfo = Tooltip.create(Component.literal("Offsets wrapping from 0,0 so edges can't be detected by players"));
	protected Tooltip widthInfo = Tooltip.create(Component.literal("How large your wrapped world should be in a direction"));

	private static final int WIDTH_BUTTON_LENGTH = 60;
	private static final int WIDTH_BUTTON_HEIGHT = 20;
	private static final int COLUMN_SPACING = 10;
	private static final int ROW_SPACING = 10;
	private static final int CHECKBOX_IDEAL_WIDTH = 17;

	protected CycleButton<WorldWrappingPresets.WorldWrappingPreset> presets;

	protected CycleButton wrappingSettingsButton;

	protected Button doneButton;

	protected EditBox xWidth;
	protected EditBox zWidth;

	protected EditBox xOffset;
	protected EditBox zOffset;

	protected Checkbox wrapX;
	protected Checkbox wrapZ;
	protected Checkbox shifting;

	protected MutableCycleButton<Integer> netherScale;
	protected CycleButton<Boolean> useWrappedWorldGen;

	protected StringWidget maxOverworldViewDist;
	protected StringWidget maxNetherViewDist;

	protected CycleButton<DimensionWrappingSettings.Axis> shiftAxis;
	protected EditBox shiftAmount;

	protected Button screenSwitch;

	private GridLayout.RowHelper bottomButtons;
	private GridLayout.RowHelper mainRow;

	/**
	 *
	 * @param createWorldScreen returned to when the wrapping settings are approved
	 * @param wrappingSettingsButton set to true if wrapping settings were entered and approved. False otherwise.
	 */
	public WorldWrappingSettingsScreen(CreateWorldScreen createWorldScreen, CycleButton wrappingSettingsButton) {
		super(Component.literal("None"));
		this.createWorldScreen = createWorldScreen;
		this.wrappingSettingsButton = wrappingSettingsButton;
	}

	/**
	 * Adds a bottom divider and darkens behind info text.
	 */
	@Override
	protected void renderMenuBackground(GuiGraphics guiGraphics) {
		super.renderMenuBackground(guiGraphics);

		GridLayout grid = mainRow.getGrid();
		//guiGraphics.fill(RenderType.gui(), 10, 10, this.width, this.height, ((alpha << 24) | (red << 16) | (green << 8) | blue)-Integer.MIN_VALUE);
		RenderSystem.enableBlend();
		guiGraphics.fill(grid.getX()+grid.getWidth()/2+10, grid.getY()+20, grid.getX()+grid.getWidth(), grid.getY()+grid.getHeight(), 2130969605);
		guiGraphics.blit(
			Screen.FOOTER_SEPARATOR, 0, this.height-(this.height-bottomButtons.getGrid().getY()+9), 0.0F, 0.0F, this.width, 2, 32, 2
		);
		RenderSystem.disableBlend();
	}

	/**
	 * Repositions elements when rescaling.
	 */
	@Override
	protected void repositionElements() {
		//Realign bottom buttons
		bottomButtons.getGrid().setPosition(this.width / 2 - 155, this.height - 26);
		bottomButtons.getGrid().arrangeElements();

		//Realign main row
		mainRow.getGrid().arrangeElements();
		ScreenRectangle rectangle = new ScreenRectangle(0, 3, this.width, this.height-(this.height-bottomButtons.getGrid().getY()+9));
		FrameLayout.alignInRectangle(mainRow.getGrid(), rectangle, 0.5F, 0.16666667F);
	}

	/**
	 * Initialize the screen and its contents.
	 */
	@Override
	protected void init() {
		//Assign variables
		initConst();

		//Get initial layout
		initElements(getSimpleSettings());

		//Assign the layout an initial preset
		this.initPreset(WorldWrappingPresets.getPresets().get(0));

		guiUpdate();
	}

	public void guiUpdate() {
		this.refreshInfo();
		this.checkRequirements();

		this.netherScale.updateValues(refreshNetherScales());
	}

	private void refreshInfo() {
		WorldWrappingPresets.WorldWrappingPreset settings = getSettings(true);

		if(settings == null) {
			this.maxOverworldViewDist.setMessage(Component.literal("-"));
			this.maxNetherViewDist.setMessage(Component.literal("-"));
			return;
		}

		Integer maxOverworldViewDistance = settings.getMaxOverworldViewDistance();
		Integer maxNetherViewDistance = settings.getMaxNetherViewDistance(this.netherScale.getValue());

		if(maxOverworldViewDistance < 0) this.maxOverworldViewDist.setMessage(Component.literal("-"));
		else this.maxOverworldViewDist.setMessage(Component.literal(maxOverworldViewDistance.toString()));

		if(maxNetherViewDistance < 0) this.maxNetherViewDist.setMessage(Component.literal("-"));
		else this.maxNetherViewDist.setMessage(Component.literal(maxNetherViewDistance.toString()));
	}

	private void initConst() {
		this.xWidth = getNumericEditBox(CoordinateConstants.MIN_LEVEL_WIDTH, Integer.MAX_VALUE);
		this.xWidth.setResponder((string) -> this.guiUpdate());
		this.zWidth = getNumericEditBox(CoordinateConstants.MIN_LEVEL_WIDTH, Integer.MAX_VALUE);
		this.zWidth.setResponder((string) -> this.guiUpdate());
		this.xOffset = getNumericEditBox(-CoordinateConstants.DISABLING_CHUNK_POS, CoordinateConstants.DISABLING_CHUNK_POS);
		this.zOffset = getNumericEditBox(-CoordinateConstants.DISABLING_CHUNK_POS, CoordinateConstants.DISABLING_CHUNK_POS);
		this.shiftAxis = CycleButton.<DimensionWrappingSettings.Axis>builder(i -> Component.literal(String.valueOf(i)))
			.withValues(DimensionWrappingSettings.Axis.values())
			.displayOnlyValue()
			.create(0, 0, WIDTH_BUTTON_LENGTH, WIDTH_BUTTON_HEIGHT, Component.empty()
		);
		this.shiftAmount = getNumericEditBox(-CoordinateConstants.DISABLING_CHUNK_POS, CoordinateConstants.DISABLING_CHUNK_POS);
		this.doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
			this.wrappingSettingsButton.setValue(true);
			((WorldWrappingSettingsAccessor) (Object) createWorldScreen).setWorldWrappingSettings(getWrappingSettings());
			this.minecraft.setScreen(createWorldScreen);

		}).build();
		this.presets = CycleButton.builder(WorldWrappingPresets.WorldWrappingPreset::getName)
			.withValues(WorldWrappingPresets.getPresets())
			.create(0, 0, 170, 20, Component.literal("Preset"), (button, preset) -> this.initPreset(preset));
		this.useWrappedWorldGen = CycleButton.onOffBuilder(true)
			.displayOnlyValue()
			.create(0, 0, WIDTH_BUTTON_LENGTH, WIDTH_BUTTON_HEIGHT, Component.empty()
		);
		this.netherScale = MutableCycleButton.<Integer>builder(i -> Component.literal(String.valueOf(i)))
			.withValues(List.of(1))
			.displayOnlyValue()
			.create(0, 0, WIDTH_BUTTON_LENGTH, WIDTH_BUTTON_HEIGHT, Component.empty(), (cycleButton, object) -> this.guiUpdate()
		);
	}

	protected List<Integer> refreshNetherScales() {
		WorldWrappingPresets.WorldWrappingPreset settings = getSettings(true);
		if(settings == null) return List.of(1);

		List<Integer> netherScales = settings.getValidNetherScales();
		if(netherScales.isEmpty()) return List.of(1);
		return netherScales;
	}

	protected void initElements(GridLayout.RowHelper settings) {
		//Init bottom buttons
		bottomButtons = getBottomButtons();
		bottomButtons.getGrid().visitWidgets(this::addRenderableWidget);

		//Init main Row
		mainRow = getStandardGrid(20, 0, 2);
		mainRow.addChild(settings.getGrid(), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyLeft());
		mainRow.addChild(getInfoRow().getGrid(), new LayoutSettings.LayoutSettingsImpl().alignHorizontallyRight());
		mainRow.getGrid().visitWidgets(this::addRenderableWidget);

		this.repositionElements();
	}

	protected WorldWrappingSettings getWrappingSettings() {
		WorldWrappingPresets.WorldWrappingPreset settings = getSettings(false);
		DimensionWrappingSettings overworld = settings.getOverworldSettings();
		DimensionWrappingSettings nether = settings.getNetherSettings();

		Map<ResourceKey<Level>, DimensionWrappingSettings> worldSettings = new HashMap<>();
		worldSettings.put(Level.OVERWORLD, overworld);
		worldSettings.put(Level.NETHER, nether);
		return new WorldWrappingSettings(new WrappingOptions(1), worldSettings);
	}

	/**
	 * A simple layout for common users.
	 */
	public GridLayout.RowHelper getSimpleSettings() {
		this.screenSwitch = Button.builder(Component.literal("Advanced Settings..."), button1 -> {
				this.clearWidgets();
				this.initElements(getAdvancedSettings());
				this.mainRow.getGrid().visitWidgets(abstractWidget -> {abstractWidget.visible = true;});
				this.guiUpdate();
			})
			.bounds(0,0, 170, 20)
			.build();

		//Main settings
		GridLayout.RowHelper settingsRow = getStandardGrid(10, 10, 1);

		//Width inputs
		GridLayout.RowHelper widthRow = getStandardGrid(48, 0, 2);
		widthRow.addChild(getLiteralString("X Width"));
		widthRow.addChild(getLiteralString("Z Width"));
		widthRow.addChild(xWidth);
		widthRow.addChild(zWidth);

		//Nether scaling button
		GridLayout.RowHelper netherScaleRow = getStandardGrid(35, 0, 2);
		netherScaleRow.addChild(getLiteralString("Nether Scaling"), centered());
		netherScaleRow.addChild(netherScale);

		//Use wrapped world gen button
		GridLayout.RowHelper worldGenRow = getStandardGrid(14,0, 2);
		worldGenRow.addChild(getLiteralString("Wrapped World Gen"), centered());
		worldGenRow.addChild(useWrappedWorldGen);

		settingsRow.addChild(presets, centered());
		settingsRow.addChild(widthRow.getGrid(), centered());
		settingsRow.addChild(netherScaleRow.getGrid(), centered());
		settingsRow.addChild(worldGenRow.getGrid(), centered());

		return settingsRow;
	}

	/**
	 * An advanced layout for inputting more fine-grained wrapping settings.
	 */
	public GridLayout.RowHelper getAdvancedSettings() {
		this.screenSwitch = Button.builder(Component.literal("Simple Settings..."), button1 -> {
				this.clearWidgets();
				this.initElements(getSimpleSettings());
				//Set settings for simple mode
				if(!this.wrapX.selected()) wrapX.onPress();
				if(!this.wrapZ.selected()) wrapZ.onPress();
				if(this.shifting.selected()) shifting.onPress();
				this.xOffset.setValue("");
				this.zOffset.setValue("");

				this.guiUpdate();
			})
			.bounds(0,0, 170, 20)
			.build();

		//Main settings
		GridLayout.RowHelper settingsRow = getStandardGrid(10, 10, 1);

		//X wrapping settings
		GridLayout.RowHelper xWidthRow = getStandardGrid(5, 0,3);
		xWidthRow.addChild(getLiteralString("Wrap X"), centered());
		StringWidget xWidthInfo = xWidthRow.addChild(getLiteralString("X Width"), centered());
		StringWidget xOffsetInfo = xWidthRow.addChild(getLiteralString("X Offset"), centered());
		wrapX = getDisablingCheckbox((checkbox, bl) -> {
			if(!bl && (!wrapZ.selected())) wrapZ.onPress();
		}, xWidthInfo, xOffsetInfo, xWidth, xOffset);
		addToGrid(xWidthRow, wrapX, xWidth, xOffset);

		//Z wrapping settings
		GridLayout.RowHelper zWidthRow = getStandardGrid(5, 0, 3);
		zWidthRow.addChild(getLiteralString("Wrap Z"), centered());
		StringWidget zWidthInfo = zWidthRow.addChild(getLiteralString("Z Width"), centered());
		StringWidget zOffsetInfo = zWidthRow.addChild(getLiteralString("Z Offset"), centered());
		wrapZ = getDisablingCheckbox((checkbox, bl) -> {
			if(!bl && (!wrapX.selected())) wrapX.onPress();
		}, zWidthInfo, zOffsetInfo, zWidth, zOffset);
		addToGrid(zWidthRow, wrapZ, zWidth, zOffset);

		//Shifting wrapping settings
		GridLayout.RowHelper shiftRow = getStandardGrid(5, 0, 3);
		shiftRow.addChild(getLiteralString("Shifting"), centered());
		StringWidget shiftAxisInfo = shiftRow.addChild(getLiteralString("Shift Axis"), centered());
		StringWidget shiftAmountInfo = shiftRow.addChild(getLiteralString("Shift Amount"), centered());
		shifting = getDisablingCheckbox((checkbox, bl) -> {}, shiftAxisInfo, shiftAmountInfo, shiftAxis, shiftAmount);
		addToGrid(shiftRow, shifting, shiftAxis, shiftAmount);

		//Nether scaling button
		GridLayout.RowHelper netherScaleRow = getStandardGrid(35, 0, 2);
		netherScaleRow.addChild(getLiteralString("Nether Scaling"), centered());
		netherScaleRow.addChild(netherScale);

		//Use wrapped world gen button
		GridLayout.RowHelper worldGenRow = getStandardGrid(14,0, 2);
		worldGenRow.addChild(getLiteralString("Wrapped World Gen"), centered());
		worldGenRow.addChild(useWrappedWorldGen);

		//Layout
		settingsRow.addChild(presets, centered());
		settingsRow.addChild(xWidthRow.getGrid(), centered());
		settingsRow.addChild(zWidthRow.getGrid(), centered());
		settingsRow.addChild(shiftRow.getGrid(), centered());
		settingsRow.addChild(netherScaleRow.getGrid(), centered());
		settingsRow.addChild(worldGenRow.getGrid(),centered());

		return settingsRow;
	}

	/**
	 * Get the right side of the screen interface, composed of info bars and layout switch buttons.
	 */
	public GridLayout.RowHelper getInfoRow() {
		GridLayout.RowHelper rightRow = getStandardGrid(0, 10, 1);

		GridLayout.RowHelper infoRow = getStandardGrid(5, 10, 2);

		StringWidget overworldVDInfo = new StringWidget(Component.literal("Overworld RenderDist:"), this.font);
		overworldVDInfo.setTooltip(Tooltip.create(Component.literal("The max Overworld View/Render Distance these settings can provide")));
		infoRow.addChild(overworldVDInfo, centered().alignHorizontallyLeft());

		maxOverworldViewDist = new StringWidget(50, 9, Component.literal("-"), this.font);
		maxOverworldViewDist.setColor(2130771712);
		infoRow.addChild(maxOverworldViewDist, centered().alignHorizontallyRight());

		StringWidget netherVDInfo = new StringWidget(Component.literal("Nether RenderDist:"), this.font);
		netherVDInfo.setTooltip(Tooltip.create(Component.literal("The max Nether View/Render Distance these settings can provide")));
		infoRow.addChild(netherVDInfo, centered().alignHorizontallyLeft());

		maxNetherViewDist = new StringWidget(50, 9, Component.literal("-"), this.font);
		maxNetherViewDist.setColor(2130771712);
		infoRow.addChild(maxNetherViewDist, centered().alignHorizontallyRight());


		rightRow.addChild(screenSwitch, centered().alignVerticallyTop());
		rightRow.addChild(infoRow.getGrid(), centered());

		return rightRow;
	}

	/**
	 * The bottom row of buttons used for screen control.
	 */
	public GridLayout.RowHelper getBottomButtons() {
		GridLayout.RowHelper bottomButtons = getStandardGrid(9, 10, 2);

		bottomButtons.addChild(doneButton);

		bottomButtons.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> {
			this.wrappingSettingsButton.setValue(false);
			this.minecraft.setScreen(createWorldScreen);
		}).build());

		return bottomButtons;
	}

	/**
	 * Initialize a wrapping preset in the screen.
	 */
	private void initPreset(WorldWrappingPresets.WorldWrappingPreset preset) {
		//Turn on both axis
		if(!(this.wrapX == null) && !this.wrapX.selected()) wrapX.onPress();
		if(!(this.wrapZ == null) && !this.wrapZ.selected()) wrapZ.onPress();

		//
		if(preset.xWidth() == CoordinateConstants.DISABLING_CHUNK_POS*2) {wrapX.onPress(); this.xWidth.setValue("");}
		else this.xWidth.setValue(String.valueOf(preset.xWidth()));
		if(preset.zWidth() == CoordinateConstants.DISABLING_CHUNK_POS*2) {wrapZ.onPress(); this.zWidth.setValue("");}
		else this.zWidth.setValue(String.valueOf(preset.zWidth()));

		if(preset.xOffset() == 0) this.xOffset.setValue("");
		else this.xOffset.setValue(String.valueOf(preset.xOffset()));
		if(preset.zOffset() == 0) this.zOffset.setValue("");
		else this.zOffset.setValue(String.valueOf(preset.zOffset()));

		if(preset.shiftAmount() == 0) {this.shiftAmount.setValue(""); if(!(this.shifting == null) && !this.shifting.selected()) {this.shifting.onPress(); this.shifting.onPress();}}
		else this.shiftAmount.setValue(String.valueOf(preset.shiftAmount()));

		this.shiftAxis.setValue(preset.shiftAxis());
		this.netherScale.setValue(preset.netherScale());
		this.useWrappedWorldGen.setValue(preset.useWrappedWorldGen());

		this.guiUpdate();
	}

	/**
	 * Ensures numbers are in their valid ranges and don't approve the settings unless they are.
	 */
	public void checkRequirements() {
		WorldWrappingPresets.WorldWrappingPreset settings = getSettings(false);
		this.doneButton.active = false;
		if(getSettings(false) == null) return;

		//Width cannot be less than the minimum
		if(settings.xWidth() < CoordinateConstants.MIN_LEVEL_WIDTH || settings.zWidth() < CoordinateConstants.MIN_LEVEL_WIDTH) return;

		//if(Math.min(settings.xWidth(), settings.zWidth())/settings.netherScale() < CoordinateConstants.MIN_LEVEL_WIDTH) return;

		this.doneButton.active = true;
	}

	/**
	 * Get the entered settings
	 * @param widthOnly only retrieve the xWidth and zWidth values
	 * @return null if the settings aren't retrievable and a WorldWrappingPreset otherwise
	 */
	private WorldWrappingPresets.WorldWrappingPreset getSettings(boolean widthOnly) {
		WorldWrappingPresets.WorldWrappingPreset.Builder builder = new WorldWrappingPresets.WorldWrappingPreset.Builder("result");

		//These are always available
		builder.setNetherScale(this.netherScale.getValue());
		builder.useWrappedWorldGen(this.useWrappedWorldGen.getValue());

		try {
			//If  wrapX is unchecked, disable the X axis
			if(this.wrapX != null && !this.wrapX.selected()) builder.setXWidth(CoordinateConstants.DISABLING_CHUNK_POS*2);
			else {
				Integer xWidth = Integer.parseInt(this.xWidth.getValue());
				builder.setXWidth(xWidth);
			}

			//If wrapZ is unchecked, disable the Z axis
			if(this.wrapZ != null && !this.wrapZ.selected()) builder.setZWidth(CoordinateConstants.DISABLING_CHUNK_POS*2);
			else {
				Integer zWidth = Integer.parseInt(this.zWidth.getValue());
				builder.setZWidth(zWidth);
			}

			if(widthOnly) return builder.build();

			//If empty, it was blank and should be default
			if(!this.xOffset.getValue().equals("")) {
				Integer xOffset = Integer.parseInt(this.xOffset.getValue());
				builder.setXOffset(xOffset);
			}

			//If empty, it was blank and should be default
			if(!this.zOffset.getValue().equals("")) {
				Integer zOffset = Integer.parseInt(this.zOffset.getValue());
				builder.setZOffset(zOffset);
			}

			//If empty or shifting is unchecked, it was blank and should be default
			if(this.shifting != null && this.shifting.selected() && !this.shiftAmount.getValue().equals("")) {
				Integer shiftAmount = Integer.parseInt(this.shiftAmount.getValue());
				builder.setShiftAmount(shiftAmount);
				builder.setShiftAxis(this.shiftAxis.getValue());
			}
		} catch (Exception e) {
			return null;
		}

		return builder.build();
	}

	/**
	 * Add centered elements to a grid helper.
	 */
	public void addToGrid(GridLayout.RowHelper list, LayoutElement... elements) {
		for(LayoutElement element : elements) {
			list.addChild(element, centered());
		}
	}

	/**
	 * A restricted EditBox meant for numerals only.
	 */
	private EditBox getNumericEditBox(int min, int max) {
		EditBox box = new EditBox(this.font, WIDTH_BUTTON_LENGTH, WIDTH_BUTTON_HEIGHT, Component.literal(""));

		//Must be numeric or empty. Must be empty or not start with the numeral 0.
		box.setFilter(i -> StringUtils.isNumeric(i) || i.isEmpty() || i.charAt(0) != '0');
		box.setMaxLength(6);
		return box;
	}

	/**
	 * Get a controller checkbox which hides inputted widgets when deselected.
	 * @param onValueChange to execute when toggled.
	 * @param widgets to hide when unchecked.
	 */
	public Checkbox getDisablingCheckbox(Checkbox.OnValueChange onValueChange, AbstractWidget... widgets) {
		return Checkbox.builder(Component.empty(), this.font)
			.selected(true)
			.onValueChange((checkbox, bl) -> {
				onValueChange.onValueChange(checkbox, bl);
				for(AbstractWidget widget : widgets) {
					widget.visible = bl;
				}
			})
			.maxWidth(CHECKBOX_IDEAL_WIDTH)
			.build();
	}

	/**
	 * Easy way to get Components of strings.
	 */
	public StringWidget getLiteralString(String string) {
		return new StringWidget(Component.literal(string), this.font);
	}

	/**
	 * A centered layout.
	 */
	public LayoutSettings centered() {
		return new LayoutSettings.LayoutSettingsImpl().alignHorizontallyCenter().alignVerticallyMiddle();
	}

	/**
	 * For squished instance calls.
	 */
	public GridLayout.RowHelper getStandardGrid(int columnSpacing, int rowSpacing, int columns) {
		return new GridLayout().columnSpacing(columnSpacing).rowSpacing(rowSpacing).createRowHelper(columns);
	}
}
