/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packet;

import com.fexl.circumnavigate.processing.PacketTransformer;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
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
