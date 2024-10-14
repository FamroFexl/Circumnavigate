/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transforms packets into their wrapped counterparts.
 */
@SuppressWarnings({"unused", "unchecked"})
public class PacketTransformer {
	private static final Map<Class<? extends Packet<?>>, Method> methodCache = new HashMap<>();

	static Logger LOGGER = LogUtils.getLogger();

	static {
		Method[] methods = PacketTransformer.class.getDeclaredMethods();

		for(Method method : methods) {
			if(method.getName().equals("transformPacket")) {
				Class<?>[] parameters = method.getParameterTypes();
				if(parameters.length == 2) {
					if(Packet.class.isAssignableFrom(parameters[0]) && parameters[1] == ServerPlayer.class) {
						PacketTransformer.methodCache.put((Class<? extends Packet<?>>) parameters[0], method);
					}
				}
			}
		}
	}

	public static Packet<?> process(Packet<?> packet, ServerPlayer player){
		Method transformedPacket = methodCache.get(packet.getClass());

		//A method does not exist to handle the packet. Don't transform it
		if(transformedPacket == null) {
			return packet;
		}
		try {
			//Process the packet with its transformer method. TODO find a way to automatically register all the overrided transformPacket methods without the use of reflection. Reflection invoking is slow.
			return (Packet<?>) transformedPacket.invoke(PacketTransformer.class, packet, player);
		//This should never occur.
		} catch (InvocationTargetException | IllegalAccessException e) {
			LOGGER.error("{} couldn't be processed by the transformer!", packet.getClass().getSimpleName());
			return packet;
		}
	}

	private static ClientboundLightUpdatePacket transformPacket(ClientboundLightUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		ChunkPos newPos = player.serverLevel().getTransformer().translateChunkFromBounds(player.getClientChunk(), new ChunkPos(packet.getX(), packet.getZ()));
		buffer.writeVarInt(newPos.x);
		buffer.writeVarInt(newPos.x);
		packet.getLightData().write(buffer);
		return new ClientboundLightUpdatePacket(buffer);
	}

	private static ClientboundSetChunkCacheCenterPacket transformPacket(ClientboundSetChunkCacheCenterPacket packet, ServerPlayer player) {
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

	private static ClientboundOpenSignEditorPacket transformPacket(ClientboundOpenSignEditorPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(player.serverLevel().getTransformer().translateBlockFromBounds(player.getClientBlock(), packet.getPos()));
		buffer.writeBoolean(packet.isFrontText());
		return new ClientboundOpenSignEditorPacket(buffer);
	}


	private static ClientboundBlockEventPacket transformPacket(ClientboundBlockEventPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(player.serverLevel().getTransformer().translateBlockFromBounds(player.getClientBlock(), packet.getPos()));
		buffer.writeByte(packet.getB0());
		buffer.writeByte(packet.getB1());
		buffer.writeId(BuiltInRegistries.BLOCK, packet.getBlock());
		return new ClientboundBlockEventPacket(buffer);
	}

	private static ClientboundForgetLevelChunkPacket transformPacket(ClientboundForgetLevelChunkPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeChunkPos(player.serverLevel().getTransformer().translateChunkFromBounds(player.getClientChunk(), packet.pos()));
		return new ClientboundForgetLevelChunkPacket(buffer);
	}

	private static ClientboundBlockUpdatePacket transformPacket(ClientboundBlockUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(player.serverLevel().getTransformer().translateBlockFromBounds(player.getClientBlock(), packet.getPos()));
		buffer.writeId(Block.BLOCK_STATE_REGISTRY, packet.getBlockState());
		return new ClientboundBlockUpdatePacket(buffer);
	}

	private static ClientboundPlayerPositionPacket transformPacket(ClientboundPlayerPositionPacket packet, ServerPlayer player) {
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


	private static ClientboundAddEntityPacket transformPacket(ClientboundAddEntityPacket packet, ServerPlayer player) {
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
		buffer.writeShort((int) packet.getXa() / 8000);
		buffer.writeShort((int) packet.getYa() / 8000);
		buffer.writeShort((int) packet.getZa() / 8000);
		return new ClientboundAddEntityPacket(buffer);
	}

	private static ClientboundTeleportEntityPacket transformPacket(ClientboundTeleportEntityPacket packet, ServerPlayer player) {
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


	private static ClientboundBundlePacket transformPacket(ClientboundBundlePacket packet, ServerPlayer player) {
		List<Packet<ClientGamePacketListener>> outputPackets = new ArrayList<>();
		packet.subPackets().forEach((subPacket) -> {
			outputPackets.add((Packet<ClientGamePacketListener>) PacketTransformer.process(subPacket, player));
		});

		return new ClientboundBundlePacket(outputPackets);


	}
}
