/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DimensionWrappingSettings(int xChunkBoundMin, int xChunkBoundMax, int zChunkBoundMin, int zChunkBoundMax, Axis shiftAxis, int shiftAmount, boolean useWrappedWorldGen) {
	public static final Codec<DimensionWrappingSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				Codec.INT.fieldOf("XChunkBoundMin").forGetter(DimensionWrappingSettings::xChunkBoundMin),
				Codec.INT.fieldOf("XChunkBoundMax").forGetter(DimensionWrappingSettings::xChunkBoundMax),
				Codec.INT.fieldOf("ZChunkBoundMin").forGetter(DimensionWrappingSettings::zChunkBoundMin),
				Codec.INT.fieldOf("ZChunkBoundMax").forGetter(DimensionWrappingSettings::zChunkBoundMax),
				Axis.CODEC.fieldOf("ShiftAxis").forGetter(DimensionWrappingSettings::shiftAxis),
				Codec.INT.fieldOf("ShiftAmount").forGetter(DimensionWrappingSettings::shiftAmount),
				Codec.BOOL.fieldOf("UseWrappedWorldGen").forGetter(DimensionWrappingSettings::useWrappedWorldGen)
			)
			.apply(instance, instance.stable(DimensionWrappingSettings::new))
	);

	public enum Axis {
		X,
		Z;

		public static final Codec<DimensionWrappingSettings.Axis> CODEC = Codec.STRING.comapFlatMap(
			axis -> {
				try {
					return DataResult.success(DimensionWrappingSettings.Axis.valueOf(axis));
				} catch (IllegalArgumentException e) {
					return DataResult.error(() -> "\"" + axis + "\" is not an axis!");
				}
			},
			DimensionWrappingSettings.Axis::toString
		);
	}
}
