/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.network.packet;

import com.fexl.circumnavigate.options.DimensionWrappingSettings;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Sends clients dimension wrapping data.
 */
public record DimensionWrappingPayload(ResourceKey<Level> levelKey, DimensionWrappingSettings wrappingSettings) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, DimensionWrappingPayload> STREAM_CODEC = CustomPacketPayload.codec(DimensionWrappingPayload::write, DimensionWrappingPayload::new);
	public static final CustomPacketPayload.Type<DimensionWrappingPayload> TYPE = CustomPacketPayload.createType("debug/circumnavigate/wrapping_data");

	private DimensionWrappingPayload(FriendlyByteBuf buffer) {
		this(buffer.readResourceKey(Registries.DIMENSION), new DimensionWrappingSettings(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readEnum(DimensionWrappingSettings.Axis.class), buffer.readInt(), false));
	}

	private void write(FriendlyByteBuf buffer) {
		buffer.writeResourceKey(levelKey);
		buffer.writeInt(wrappingSettings.xChunkBoundMin());
		buffer.writeInt(wrappingSettings.xChunkBoundMax());
		buffer.writeInt(wrappingSettings.zChunkBoundMin());
		buffer.writeInt(wrappingSettings.zChunkBoundMax());
		buffer.writeEnum(wrappingSettings.shiftAxis());
		buffer.writeInt(wrappingSettings.shiftAmount());
	}

	@Override
	public CustomPacketPayload.Type<DimensionWrappingPayload> type() {
		return TYPE;
	}
}
