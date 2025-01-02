/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Map;

public record WorldWrappingSettings(WrappingOptions options, Map<ResourceKey<Level>, DimensionWrappingSettings> dimensions) {
	public static final Codec<WorldWrappingSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(WrappingOptions.CODEC.forGetter(WorldWrappingSettings::options), Codec.unboundedMap(ResourceKey.codec(Registries.DIMENSION), DimensionWrappingSettings.CODEC).fieldOf("dimensions").forGetter(WorldWrappingSettings::dimensions))
			.apply(instance, instance.stable(WorldWrappingSettings::new))
	);
}
