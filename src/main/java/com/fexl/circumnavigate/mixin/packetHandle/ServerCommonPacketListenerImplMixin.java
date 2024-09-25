/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packetHandle;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
		process(packet, player);
	}

	private ServerPlayer getPlayerByConnection(Connection connection) {
		for(ServerPlayer player : server.getPlayerList().getPlayers()) {
			if(player.connection.connection.equals(connection) ) {
				return player;
			}
		}
		return null;
	}

	private void process(Packet<?> packet, ServerPlayer player) {
		Packet<?> processed;
		/**
		try {
			Method transformedPacket = this.getClass().getDeclaredMethod("transformPacket", packet.getClass(), player.getClass());
			//transformedPacket.invoke(packet, player);
			System.out.println(packet.getClass() + " transformed");
		} catch (NoSuchMethodException e) {
			System.out.println(packet.getClass() + " NOT TRANSFORMED");
			send(packet, null);
		} //catch (IllegalAccessException | InvocationTargetException e) {
			//System.out.println("Invoke failed");
		//}**/

		if(packet instanceof ClientboundLightUpdatePacket) {
			processed = transformPacket((ClientboundLightUpdatePacket) packet, player);
		}
		else if(packet instanceof ClientboundSetChunkCacheCenterPacket) {
			processed = transformPacket((ClientboundSetChunkCacheCenterPacket) packet, player);
		}
		else if(packet instanceof ClientboundOpenSignEditorPacket) {
			processed = transformPacket((ClientboundOpenSignEditorPacket) packet, player);
		}
		else if(packet instanceof ClientboundBlockEventPacket) {
			processed = transformPacket((ClientboundBlockEventPacket) packet, player);
		}
		else if(packet instanceof ClientboundForgetLevelChunkPacket) {
			processed = transformPacket((ClientboundForgetLevelChunkPacket) packet, player);
		}
		else if(packet instanceof ClientboundBlockUpdatePacket) {
			processed = transformPacket((ClientboundBlockUpdatePacket) packet, player);
		}
		else if(packet instanceof ClientboundPlayerPositionPacket) {
			processed = transformPacket((ClientboundPlayerPositionPacket) packet, player);
		}
		else if(packet instanceof ClientboundAddEntityPacket) {
			processed = transformPacket((ClientboundAddEntityPacket) packet, player);
		}
		else if(packet instanceof ClientboundTeleportEntityPacket) {
			processed = transformPacket((ClientboundTeleportEntityPacket) packet, player);
		}
		else {
			send(packet, null);
			return;
		}
		send(processed, null);
	}

	private ClientboundLightUpdatePacket transformPacket(ClientboundLightUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		ChunkPos newPos = player.serverLevel().getTransformer().translateChunkFromBounds(player.getClientChunk(), new ChunkPos(packet.getX(), packet.getZ()));
		buffer.writeVarInt(newPos.x);
		buffer.writeVarInt(newPos.x);
		packet.getLightData().write(buffer);
		return new ClientboundLightUpdatePacket(buffer);
	}

	private ClientboundSetChunkCacheCenterPacket transformPacket(ClientboundSetChunkCacheCenterPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		ChunkPos newPos = player.serverLevel().getTransformer().translateChunkFromBounds(player.getClientChunk(), new ChunkPos(packet.getX(), packet.getZ()));
		buffer.writeVarInt(newPos.x);
		buffer.writeVarInt(newPos.z);
		return new ClientboundSetChunkCacheCenterPacket(buffer);
	}
	/**
	private ClientboundSoundPacket transformPacket(ClientboundSoundPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), packet.getSound(), ((friendlyByteBuf, soundEvent) -> soundEvent.writeToNetwork(friendlyByteBuf)));
		buffer.writeEnum(packet.getSource());

	}**/

	private ClientboundOpenSignEditorPacket transformPacket(ClientboundOpenSignEditorPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(player.serverLevel().getTransformer().translateBlockFromBounds(player.getClientBlock(), packet.getPos()));
		buffer.writeBoolean(packet.isFrontText());
		return new ClientboundOpenSignEditorPacket(buffer);
	}


	private ClientboundBlockEventPacket transformPacket(ClientboundBlockEventPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(player.serverLevel().getTransformer().translateBlockFromBounds(player.getClientBlock(), packet.getPos()));
		buffer.writeByte(packet.getB0());
		buffer.writeByte(packet.getB1());
		buffer.writeId(BuiltInRegistries.BLOCK, packet.getBlock());
		return new ClientboundBlockEventPacket(buffer);
	}

	private ClientboundForgetLevelChunkPacket transformPacket(ClientboundForgetLevelChunkPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeChunkPos(player.serverLevel().getTransformer().translateChunkFromBounds(player.getClientChunk(), packet.pos()));
		return new ClientboundForgetLevelChunkPacket(buffer);
	}

	private ClientboundBlockUpdatePacket transformPacket(ClientboundBlockUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(player.serverLevel().getTransformer().translateBlockFromBounds(player.getClientBlock(), packet.getPos()));
		buffer.writeId(Block.BLOCK_STATE_REGISTRY, packet.getBlockState());
		return new ClientboundBlockUpdatePacket(buffer);
	}

	private ClientboundPlayerPositionPacket transformPacket(ClientboundPlayerPositionPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeDouble(player.serverLevel().getTransformer().xTransformer.unwrapCoordFromLimit(player.getClientX(), packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(player.serverLevel().getTransformer().zTransformer.unwrapCoordFromLimit(player.getClientZ(), packet.getZ()));
		buffer.writeFloat(packet.getYRot());
		buffer.writeFloat(packet.getXRot());
		buffer.writeByte(RelativeMovement.pack(packet.getRelativeArguments()));
		buffer.writeVarInt(packet.getId());
		return new ClientboundPlayerPositionPacket(buffer);
	}

	private ClientboundAddEntityPacket transformPacket(ClientboundAddEntityPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeVarInt(packet.getId());
		buffer.writeUUID(packet.getUUID());
		buffer.writeId(BuiltInRegistries.ENTITY_TYPE, packet.getType());
		buffer.writeDouble(player.serverLevel().getTransformer().xTransformer.unwrapCoordFromLimit(player.getClientX(), packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(player.serverLevel().getTransformer().zTransformer.unwrapCoordFromLimit(player.getClientZ(), packet.getZ()));
		buffer.writeByte((int) ((packet.getXRot() * 256.0) / 360));
		buffer.writeByte((int) ((packet.getYRot() * 256.0) / 360));
		buffer.writeByte((int) ((packet.getYHeadRot() * 256.0) / 360));
		buffer.writeVarInt(packet.getData());
		buffer.writeShort((int) packet.getXa() * 8000);
		buffer.writeShort((int) packet.getYa() * 8000);
		buffer.writeShort((int) packet.getZa() * 8000);
		return new ClientboundAddEntityPacket(buffer);
	}

	private ClientboundTeleportEntityPacket transformPacket(ClientboundTeleportEntityPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeVarInt(packet.getId());
		buffer.writeDouble(player.serverLevel().getTransformer().xTransformer.unwrapCoordFromLimit(player.getClientX(), packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(player.serverLevel().getTransformer().zTransformer.unwrapCoordFromLimit(player.getClientZ(), packet.getZ()));
		buffer.writeByte(packet.getyRot());
		buffer.writeByte(packet.getxRot());
		buffer.writeBoolean(packet.isOnGround());
		return new ClientboundTeleportEntityPacket(buffer);
	}






}
