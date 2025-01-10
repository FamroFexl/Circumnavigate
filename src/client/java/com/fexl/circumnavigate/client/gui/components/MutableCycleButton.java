/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.client.gui.components;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * A CycleButton with an {@code updateValues()} function to update {@code values}
 */
public class MutableCycleButton<T> extends AbstractButton {
	public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
	private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
	private final Component name;
	private int index;
	private T value;
	private MutableCycleButton.ValueListSupplier<T> values;
	private final Function<T, Component> valueStringifier;
	private final Function<MutableCycleButton<T>, MutableComponent> narrationProvider;
	private final MutableCycleButton.OnValueChange<T> onValueChange;
	private final boolean displayOnlyValue;
	private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

	MutableCycleButton(
		int x,
		int y,
		int width,
		int height,
		Component message,
		Component name,
		int index,
		T value,
		MutableCycleButton.ValueListSupplier<T> values,
		Function<T, Component> valueStringifier,
		Function<MutableCycleButton<T>, MutableComponent> narrationProvider,
		MutableCycleButton.OnValueChange<T> onValueChange,
		OptionInstance.TooltipSupplier<T> tooltipSupplier,
		boolean displayOnlyValue
	) {
		super(x, y, width, height, message);
		this.name = name;
		this.index = index;
		this.value = value;
		this.values = values;
		this.valueStringifier = valueStringifier;
		this.narrationProvider = narrationProvider;
		this.onValueChange = onValueChange;
		this.displayOnlyValue = displayOnlyValue;
		this.tooltipSupplier = tooltipSupplier;
		this.updateTooltip();
	}

	private void updateTooltip() {
		this.setTooltip(this.tooltipSupplier.apply(this.value));
	}

	@Override
	public void onPress() {
		if (Screen.hasShiftDown()) {
			this.cycleValue(-1);
		} else {
			this.cycleValue(1);
		}
	}

	private void cycleValue(int delta) {
		List<T> list = this.values.getSelectedList();
		this.index = Mth.positiveModulo(this.index + delta, list.size());
		T object = (T)list.get(this.index);
		this.updateValue(object);
		this.onValueChange.onValueChange(this, object);
	}

	private T getCycledValue(int delta) {
		List<T> list = this.values.getSelectedList();
		return (T)list.get(Mth.positiveModulo(this.index + delta, list.size()));
	}

	public void updateValues(Collection<T> values) {
		this.values = MutableCycleButton.ValueListSupplier.create(values);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY > 0.0) {
			this.cycleValue(-1);
		} else if (scrollY < 0.0) {
			this.cycleValue(1);
		}

		return true;
	}

	public void setValue(T value) {
		List<T> list = this.values.getSelectedList();
		int i = list.indexOf(value);
		if (i != -1) {
			this.index = i;
		}

		this.updateValue(value);
	}

	private void updateValue(T value) {
		Component component = this.createLabelForValue(value);
		this.setMessage(component);
		this.value = value;
		this.updateTooltip();
	}

	private Component createLabelForValue(T value) {
		return (Component)(this.displayOnlyValue ? (Component)this.valueStringifier.apply(value) : this.createFullName(value));
	}

	private MutableComponent createFullName(T value) {
		return CommonComponents.optionNameValue(this.name, (Component)this.valueStringifier.apply(value));
	}

	public T getValue() {
		return this.value;
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return (MutableComponent)this.narrationProvider.apply(this);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if (this.active) {
			T object = this.getCycledValue(1);
			Component component = this.createLabelForValue(object);
			if (this.isFocused()) {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", new Object[]{component}));
			} else {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", new Object[]{component}));
			}
		}
	}

	public MutableComponent createDefaultNarrationMessage() {
		return wrapDefaultNarrationMessage((Component)(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
	}

	public static <T> MutableCycleButton.Builder<T> builder(Function<T, Component> valueStringifier) {
		return new MutableCycleButton.Builder<>(valueStringifier);
	}

	public static MutableCycleButton.Builder<Boolean> booleanBuilder(Component componentOn, Component componentOff) {
		return new MutableCycleButton.Builder<Boolean>(boolean_ -> boolean_ ? componentOn : componentOff).withValues(BOOLEAN_OPTIONS);
	}

	public static MutableCycleButton.Builder<Boolean> onOffBuilder() {
		return new MutableCycleButton.Builder<Boolean>(boolean_ -> boolean_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).withValues(BOOLEAN_OPTIONS);
	}

	public static MutableCycleButton.Builder<Boolean> onOffBuilder(boolean initialValue) {
		return onOffBuilder().withInitialValue(initialValue);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder<T> {
		private int initialIndex;
		@Nullable
		private T initialValue;
		private final Function<T, Component> valueStringifier;
		private OptionInstance.TooltipSupplier<T> tooltipSupplier = object -> null;
		private Function<MutableCycleButton<T>, MutableComponent> narrationProvider = MutableCycleButton::createDefaultNarrationMessage;
		private MutableCycleButton.ValueListSupplier<T> values = MutableCycleButton.ValueListSupplier.create(ImmutableList.<T>of());
		private boolean displayOnlyValue;

		public Builder(Function<T, Component> valueStringifier) {
			this.valueStringifier = valueStringifier;
		}

		public MutableCycleButton.Builder<T> withValues(Collection<T> values) {
			return this.withValues(MutableCycleButton.ValueListSupplier.create(values));
		}

		@SafeVarargs
		public final MutableCycleButton.Builder<T> withValues(T... values) {
			return this.withValues(ImmutableList.<T>copyOf(values));
		}

		public MutableCycleButton.Builder<T> withValues(List<T> defaultList, List<T> selectedList) {
			return this.withValues(MutableCycleButton.ValueListSupplier.create(MutableCycleButton.DEFAULT_ALT_LIST_SELECTOR, defaultList, selectedList));
		}

		public MutableCycleButton.Builder<T> withValues(BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
			return this.withValues(MutableCycleButton.ValueListSupplier.create(altListSelector, defaultList, selectedList));
		}

		public MutableCycleButton.Builder<T> withValues(MutableCycleButton.ValueListSupplier<T> values) {
			this.values = values;
			return this;
		}

		public MutableCycleButton.Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> tooltipSupplier) {
			this.tooltipSupplier = tooltipSupplier;
			return this;
		}

		public MutableCycleButton.Builder<T> withInitialValue(T initialValue) {
			this.initialValue = initialValue;
			int i = this.values.getDefaultList().indexOf(initialValue);
			if (i != -1) {
				this.initialIndex = i;
			}

			return this;
		}

		public MutableCycleButton.Builder<T> withCustomNarration(Function<MutableCycleButton<T>, MutableComponent> narrationProvider) {
			this.narrationProvider = narrationProvider;
			return this;
		}

		public MutableCycleButton.Builder<T> displayOnlyValue() {
			this.displayOnlyValue = true;
			return this;
		}

		public MutableCycleButton<T> create(Component message, MutableCycleButton.OnValueChange<T> onValueChange) {
			return this.create(0, 0, 150, 20, message, onValueChange);
		}

		public MutableCycleButton<T> create(int x, int y, int width, int height, Component name) {
			return this.create(x, y, width, height, name, (cycleButton, object) -> {
			});
		}

		public MutableCycleButton<T> create(int x, int y, int width, int height, Component name, MutableCycleButton.OnValueChange<T> onValueChange) {
			List<T> list = this.values.getDefaultList();
			if (list.isEmpty()) {
				throw new IllegalStateException("No values for cycle button");
			} else {
				T object = (T)(this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
				Component component = (Component)this.valueStringifier.apply(object);
				Component component2 = (Component)(this.displayOnlyValue ? component : CommonComponents.optionNameValue(name, component));
				return new MutableCycleButton<>(
					x,
					y,
					width,
					height,
					component2,
					name,
					this.initialIndex,
					object,
					this.values,
					this.valueStringifier,
					this.narrationProvider,
					onValueChange,
					this.tooltipSupplier,
					this.displayOnlyValue
				);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public interface OnValueChange<T> {
		void onValueChange(MutableCycleButton<T> cycleButton, T object);
	}

	@Environment(EnvType.CLIENT)
	public interface ValueListSupplier<T> {
		List<T> getSelectedList();

		List<T> getDefaultList();

		static <T> MutableCycleButton.ValueListSupplier<T> create(Collection<T> values) {
			final List<T> list = ImmutableList.copyOf(values);
			return new MutableCycleButton.ValueListSupplier<T>() {
				@Override
				public List<T> getSelectedList() {
					return list;
				}

				@Override
				public List<T> getDefaultList() {
					return list;
				}
			};
		}

		static <T> MutableCycleButton.ValueListSupplier<T> create(BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
			final List<T> list = ImmutableList.copyOf(defaultList);
			final List<T> list2 = ImmutableList.copyOf(selectedList);
			return new MutableCycleButton.ValueListSupplier<T>() {
				@Override
				public List<T> getSelectedList() {
					return altListSelector.getAsBoolean() ? list2 : list;
				}

				@Override
				public List<T> getDefaultList() {
					return list;
				}
			};
		}
	}
}
