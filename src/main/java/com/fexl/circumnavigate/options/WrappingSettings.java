/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.options;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

/**
 * Used for retrieving wrapping data from serialized json in the level.dat
 */
public final class WrappingSettings {

	private static WrappingSettings instance;

	/**
	 * A value well-beyond vanilla chunk limits to preclude any possibility of a transformer executing when it shouldn't (excluding the use of mods).
	 * Used for turning off wrapping transformers when in vanilla worlds.
	 */
	public static final int invalidPos = ChunkPos.getX(ChunkPos.INVALID_CHUNK_POS)+10000;

	/**
	 * Meant to crash the world loading if wrapping settings are corrupt.
	 */
	public static final WrappingSettings CORRUPT = new WrappingSettings(-1,-2,-1,-2, Axis.EMPTY, -1);

	/**
	 * Used to disable world wrapping.
	 */
	public static final WrappingSettings DISABLED = new WrappingSettings(-invalidPos, invalidPos, -invalidPos, invalidPos, Axis.X, 0);

	private final int xChunkBoundMin;
	private final int xChunkBoundMax;

	private final int zChunkBoundMin;
	private final int zChunkBoundMax;

	private final WrappingSettings.Axis axis;

	private final int axisShift;

	public WrappingSettings(int xChunkBoundMin, int xChunkBoundMax, int zChunkBoundMin, int zChunkBoundMax, Axis axis, int axisShift) {
		this.xChunkBoundMin = xChunkBoundMin;
		this.xChunkBoundMax = xChunkBoundMax;

		this.zChunkBoundMin = zChunkBoundMin;
		this.zChunkBoundMax = zChunkBoundMax;

		this.axis = axis;
		this.axisShift = axisShift;

		instance = this;
	}

	public WrappingSettings(WrappingSettings settings) {
		this(settings.xChunkBoundMin, settings.xChunkBoundMax, settings.zChunkBoundMin, settings.zChunkBoundMax, settings.axis, settings.axisShift);
	}

	public static WrappingSettings parse(Dynamic<?> wrappingData) {
		Dynamic<?> worldWrapping = wrappingData.get("WorldWrapping").orElseEmptyList();

		//If the WrappingSettings don't exist in the level.dat
		if(worldWrapping.emptyList().equals(worldWrapping)) {
			return new WrappingSettings(WrappingSettings.DISABLED);
		}

		//Don't load the world (return to menu) if the wrapping settings exist, but they are corrupt.
		int xChunkBoundMin = worldWrapping.get("xChunkBoundMin").asInt(invalidPos);
		int xChunkBoundMax = worldWrapping.get("xChunkBoundMax").asInt(invalidPos);
		int zChunkBoundMin = worldWrapping.get("zChunkBoundMin").asInt(invalidPos);
		int zChunkBoundMax = worldWrapping.get("zChunkBoundMax").asInt(invalidPos);
		int xShift = worldWrapping.get("xShift").asInt(invalidPos);
		int zShift = worldWrapping.get("zShift").asInt(invalidPos);

		//Values don't all exist, or they are wrong
		if(xChunkBoundMin == invalidPos || xChunkBoundMax == invalidPos || zChunkBoundMin == invalidPos || zChunkBoundMax == invalidPos || xShift == invalidPos || zShift == invalidPos) {
			return WrappingSettings.CORRUPT;
		}
		//Make sure min < max for x and z. and either xShift or zShift are 0
		if(xChunkBoundMin > xChunkBoundMax || zChunkBoundMin > zChunkBoundMax || !(xShift == 0 || zShift == 0)) {
			return WrappingSettings.CORRUPT;
		}

		//TODO: Check that minimum and maximum bounds are not breached.

		if(xShift != 0)
			return new WrappingSettings(xChunkBoundMin, xChunkBoundMax, zChunkBoundMin, zChunkBoundMax, Axis.X, xShift);
		else
			return new WrappingSettings(xChunkBoundMin, xChunkBoundMax, zChunkBoundMin, zChunkBoundMax, Axis.Z, zShift);
	}

	public static CompoundTag serialize(CompoundTag nbt) {
		//No wrapping requested
		if(!WrappingSettings.getIsWrapped()) {
			return nbt;
		}

		CompoundTag limits = new CompoundTag();

		limits.putLong("xChunkBoundMin", WrappingSettings.getXChunkBoundMin());
		limits.putLong("xChunkBoundMax", WrappingSettings.getXChunkBoundMax());
		limits.putLong("zChunkBoundMin", WrappingSettings.getZChunkBoundMin());
		limits.putLong("zChunkBoundMax", WrappingSettings.getZChunkBoundMax());
		limits.putLong("xShift", WrappingSettings.getXShift());
		limits.putLong("zShift", WrappingSettings.getZShift());

		nbt.put("WorldWrapping", limits);
		return nbt;
	}

	public enum Axis {
		X(Component.literal("X")),
		Z(Component.literal("Z")),
		EMPTY(Component.empty());

		private final Component text;

		private Axis(Component text) {
			this.text = text;
		}

		public Component getText() {
			return this.text;
		}
	}

	public static int getXChunkBoundMin() {
		return instance.xChunkBoundMin;
	}

	public static int getXChunkBoundMax() {
		return instance.xChunkBoundMax;
	}

	public static int getZChunkBoundMin() {
		return instance.zChunkBoundMin;
	}

	public static int getZChunkBoundMax() {
		return instance.zChunkBoundMax;
	}

	public static int getXShift() {
		return (instance.axis == Axis.X ? instance.axisShift : 0);
	}

	public static int getZShift() {
		return (instance.axis == Axis.Z ? instance.axisShift : 0);
	}

	public static Axis getAxis() {
		return instance.axis;
	}

	public static int getAxisShift() {
		return instance.axisShift;
	}

	public static boolean getIsWrapped() {
		return !WrappingSettings.instance.equals(WrappingSettings.DISABLED);
	}


}
