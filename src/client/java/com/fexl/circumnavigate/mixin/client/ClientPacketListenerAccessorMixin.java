/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessorMixin {
	@Invoker("updateLevelChunk") void updateLevelChunkAM(int x, int z, ClientboundLevelChunkPacketData data);

	@Accessor("level") ClientLevel getLevel();

	@Invoker("applyLightData") void applyLightDataAM(int x, int z, ClientboundLightUpdatePacketData data);

	@Invoker("enableChunkLight") void enableChunkLightAM(LevelChunk chunk, int x, int z);

	@Invoker("queueLightRemoval") void queueLightRemovalAM(ClientboundForgetLevelChunkPacket packet);
}
