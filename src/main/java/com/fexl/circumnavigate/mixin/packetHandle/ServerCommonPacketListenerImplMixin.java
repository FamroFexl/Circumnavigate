/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packetHandle;

import com.fexl.circumnavigate.processing.PacketTransformer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	@Shadow MinecraftServer server;
	@Shadow Connection connection;

	@Shadow public abstract void send(Packet<?> packet, @Nullable PacketSendListener listener);

	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	public void sendPacket(Packet<?> packet, CallbackInfo ci) {
		ServerPlayer player = getPlayerByConnection(connection);
		if(player == null) return;

		ci.cancel();
		send(PacketTransformer.process(packet, player), null);
	}

	@Unique
	private ServerPlayer getPlayerByConnection(Connection connection) {
		for(ServerPlayer player : server.getPlayerList().getPlayers()) {
			if(player.connection.connection.equals(connection) ) {
				return player;
			}
		}
		return null;
	}






}
