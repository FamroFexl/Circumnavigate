/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.network.packet;

import com.fexl.circumnavigate.Circumnavigate;
import com.fexl.circumnavigate.util.WorldTransformer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Sends clients world wrapping data.
 */
public class ClientboundWrappingDataPacket {
	public static final ResourceLocation WRAPPING_DATA = new ResourceLocation(Circumnavigate.MOD_ID, "packet.circumnavigate.wrapping_data");
	public static void send(ServerPlayer player, HashMap<ResourceKey<Level>, WorldTransformer> transformers) {
		FriendlyByteBuf packet = PacketByteBufs.create();

		packet.writeInt(transformers.size());
		for(Map.Entry<ResourceKey<Level>, WorldTransformer> entry : transformers.entrySet()) {
			ResourceKey<Level> levelKey = entry.getKey();
			WorldTransformer transformer = entry.getValue();
			packet.writeResourceKey(levelKey);
			packet.writeInt(transformer.xChunkBoundMin);
			packet.writeInt(transformer.xChunkBoundMax);
			packet.writeInt(transformer.zChunkBoundMin);
			packet.writeInt(transformer.zChunkBoundMax);
			packet.writeInt(transformer.xShift);
			packet.writeInt(transformer.zShift);
		}

		ServerPlayNetworking.send(player, ClientboundWrappingDataPacket.WRAPPING_DATA, packet);
	}
}
