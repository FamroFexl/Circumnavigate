/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WrappingOptions(int version) {
	public static final MapCodec<WrappingOptions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				Codec.INT.fieldOf("Version").forGetter(WrappingOptions::version)
			)
			.apply(instance, instance.stable(WrappingOptions::new))
	);
}
