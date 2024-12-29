/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.network.packet;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Sends clients world wrapping data.
 */
public record LevelWrappingPayload(ResourceKey<Level> levelKey, WorldTransformer transformer) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, LevelWrappingPayload> STREAM_CODEC = CustomPacketPayload.codec(LevelWrappingPayload::write, LevelWrappingPayload::new);
	public static final CustomPacketPayload.Type<LevelWrappingPayload> TYPE = CustomPacketPayload.createType("debug/circumnavigate/wrapping_data");

	private LevelWrappingPayload(FriendlyByteBuf buffer) {
		this(buffer.readResourceKey(Registries.DIMENSION), new WorldTransformer(buffer.readChunkPos(), buffer.readChunkPos(), buffer.readInt(), buffer.readInt(), true));
	}

	private void write(FriendlyByteBuf buffer) {
		buffer.writeResourceKey(levelKey);
		buffer.writeChunkPos(new ChunkPos(transformer.xChunkBoundMin, transformer.zChunkBoundMin));
		buffer.writeChunkPos(new ChunkPos(transformer.xChunkBoundMax, transformer.zChunkBoundMax));
		buffer.writeInt(transformer.xShift);
		buffer.writeInt(transformer.zShift);
	}

	@Override
	public CustomPacketPayload.Type<LevelWrappingPayload> type() {
		return TYPE;
	}
}
