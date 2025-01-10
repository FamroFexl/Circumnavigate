/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.options;

import com.fexl.circumnavigate.core.CoordinateConstants;
import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.options.DimensionWrappingSettings.Axis;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WorldWrappingPresets {
	private static final List<WorldWrappingPreset> presets = new ArrayList<>();

	//The smallest world with a *functional* view distance
	private static final WorldWrappingPreset SMALL = new WorldWrappingPreset.Builder("SMALL").setWidth(32).setNetherScale(1).buildPreset();

	private static final WorldWrappingPreset MEDIUM = new WorldWrappingPreset.Builder("MEDIUM").setWidth(64).setNetherScale(1).buildPreset();

	//Relative expansion of Hermitcraft Season 6 (4 regions)
	private static final WorldWrappingPreset HERMITCRAFT_S6 = new WorldWrappingPreset.Builder("Hermitcraft S6").setWidth(128).setNetherScale(4).buildPreset();

	//Relative expansion of Hermitcraft Seasons 5, 8 and 9 (5 regions)
	private static final WorldWrappingPreset HERMITCRAFT_S5_S8_S9 = new WorldWrappingPreset.Builder("Hermitcraft S5/S8/S9").setWidth(160).buildPreset();

	//Relative expansion of Hermitcraft Season 7 (8 regions)
	private static final WorldWrappingPreset HERMITCRAFT_S7 = new WorldWrappingPreset.Builder("Hermitcraft S7").setWidth(256).buildPreset();

	public static List<WorldWrappingPreset> getPresets() {
		return presets;
	}

	public record WorldWrappingPreset(int xWidth, int zWidth, int xOffset, int zOffset, DimensionWrappingSettings.Axis shiftAxis, int shiftAmount, int netherScale, boolean useWrappedWorldGen, String name) {
		//Automatically adds presets created with the builder to the preset list.
		private WorldWrappingPreset(Builder builder) {
			this(builder.xWidth, builder.zWidth, builder.xOffset, builder.zOffset, builder.shiftAxis, builder.shiftAmount, builder.netherScale, builder.useWrappedWorldGen, builder.name);
		}

		public WorldWrappingPreset(int xWidth, int zWidth, int netherScale, boolean useWrappedWorldGen) {
			this(xWidth, zWidth, 0, 0, Axis.X, 0, netherScale, useWrappedWorldGen, "");
		}

		public Component getName() {
			return Component.literal(name);
		}


		public DimensionWrappingSettings getOverworldSettings() {
			int xChunkBoundMin = xOffset - xWidth/2;
			int xChunkBoundMax = xOffset + xWidth/2;
			int zChunkBoundMin = zOffset - zWidth/2;
			int zChunkBoundMax = zOffset + zWidth/2;
			return new DimensionWrappingSettings(xChunkBoundMin, xChunkBoundMax, zChunkBoundMin, zChunkBoundMax, shiftAxis, shiftAmount, useWrappedWorldGen);
		}

		public DimensionWrappingSettings getNetherSettings() {
			int xChunkBoundMin = (xOffset - xWidth/2)/this.netherScale;
			int xChunkBoundMax = (xOffset + xWidth/2)/this.netherScale;
			int zChunkBoundMin = (zOffset - zWidth/2)/this.netherScale;
			int zChunkBoundMax = (zOffset + zWidth/2)/this.netherScale;
			return new DimensionWrappingSettings(xChunkBoundMin, xChunkBoundMax, zChunkBoundMin, zChunkBoundMax, shiftAxis, shiftAmount/this.netherScale, useWrappedWorldGen);
		}

		public List<Integer> getValidNetherScales() {
			LinkedList<Integer> list = new LinkedList<>();

			int minWidth = Math.min(xWidth, zWidth);

			for(int i = 1; i <= 16; i++) {
				if((minWidth*16)%i == 0) {
					if(!(getMaxNetherViewDistance(i) < CoordinateConstants.MIN_RENDER_DISTANCE) && !(minWidth/i < CoordinateConstants.MIN_LEVEL_WIDTH)) list.add(i);
				}
			}

			return list;
		}

		public Integer getMaxOverworldViewDistance() {
			DimensionWrappingSettings transformerSettings = this.getOverworldSettings();
			DimensionTransformer transformer = new DimensionTransformer(transformerSettings, true);

			return transformer.getMaxViewDistance();
		}

		public Integer getMaxNetherViewDistance(int netherScale) {
			Integer overworldDist = getMaxOverworldViewDistance();

			return overworldDist / netherScale;
		}

		public record NetherScale(int scale) {
			public Component getScale() {
				return Component.literal(((Integer) scale).toString());
			}
		}

		public static class Builder {
			int xWidth = CoordinateConstants.DISABLING_CHUNK_POS*2;
			int zWidth = CoordinateConstants.DISABLING_CHUNK_POS*2;
			int xOffset = 0;
			int zOffset = 0;
			Axis shiftAxis = Axis.X;
			int shiftAmount = 0;
			int netherScale = 8;
			boolean useWrappedWorldGen = true;
			String name;

			public Builder(String name) {
				this.name = name;
			}

			public Builder setWidth(int width) {
				this.xWidth = width;
				this.zWidth = width;
				return this;
			}

			public Builder setXWidth(int xWidth) {
				this.xWidth = xWidth;
				return this;
			}

			public Builder setZWidth(int zWidth) {
				this.zWidth = zWidth;
				return this;
			}

			public Builder setOffset(int offset) {
				this.xOffset = offset;
				this.zOffset = offset;
				return this;
			}

			public Builder setXOffset(int xOffset) {
				this.xOffset = xOffset;
				return this;
			}

			public Builder setZOffset(int zOffset) {
				this.zOffset = zOffset;
				return this;
			}

			public Builder setShiftAxis(DimensionWrappingSettings.Axis shiftAxis) {
				this.shiftAxis = shiftAxis;
				return this;
			}

			public Builder setShiftAmount(int shiftAmount) {
				this.shiftAmount = shiftAmount;
				return this;
			}

			public Builder setNetherScale(int netherScale) {
				this.netherScale = netherScale;
				return this;
			}

			public Builder useWrappedWorldGen(boolean useWrappedWorldGen) {
				this.useWrappedWorldGen = useWrappedWorldGen;
				return this;
			}

			public WorldWrappingPreset build() {
				return new WorldWrappingPreset(this);
			}

			private WorldWrappingPreset buildPreset() {
				WorldWrappingPresets.presets.add(new WorldWrappingPreset(this));
				return new WorldWrappingPreset(this);
			}
		}
	}
}
