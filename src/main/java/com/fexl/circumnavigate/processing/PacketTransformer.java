/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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

	//Optimization to cache reflection requests.
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

	/**
	 * Uses reflection to determine which packet goes to which transformPacket method.
	 */
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

	private static WorldTransformer playerTransformer(ServerPlayer serverPlayer) {
		return serverPlayer.serverLevel().getTransformer();
	}
	private static double getClientX(ServerPlayer player, double packetX) {
		return playerTransformer(player).xTransformer.unwrapCoordFromLimit(player.getClientX(), packetX);
	}

	private static double getClientZ(ServerPlayer player, double packetZ) {
		return playerTransformer(player).zTransformer.unwrapCoordFromLimit(player.getClientZ(), packetZ);
	}

	private static int getClientX(ServerPlayer player, int packetX) {
		return playerTransformer(player).xTransformer.unwrapCoordFromLimit(player.getClientBlock().getX(), packetX);
	}

	private static int getClientZ(ServerPlayer player, int packetZ) {
		return playerTransformer(player).zTransformer.unwrapCoordFromLimit(player.getClientBlock().getZ(), packetZ);
	}

	private static ChunkPos getClientChunkPos(ServerPlayer player, ChunkPos packetChunkPos) {
		return playerTransformer(player).translateChunkFromBounds(player.getClientChunk(), packetChunkPos);
	}

	private static BlockPos getClientBlockPos(ServerPlayer player, BlockPos packetBlockPos) {
		return playerTransformer(player).translateBlockFromBounds(player.getClientBlock(), packetBlockPos);
	}

	private static ClientboundLightUpdatePacket transformPacket(ClientboundLightUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();

		ChunkPos newPos = getClientChunkPos(player, new ChunkPos(packet.getX(), packet.getZ()));
		buffer.writeVarInt(newPos.x);
		buffer.writeVarInt(newPos.z);
		packet.getLightData().write(buffer);
		return new ClientboundLightUpdatePacket(buffer);
	}

	private static ClientboundSetChunkCacheCenterPacket transformPacket(ClientboundSetChunkCacheCenterPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		ChunkPos newPos = getClientChunkPos(player, new ChunkPos(packet.getX(), packet.getZ()));
		buffer.writeVarInt(newPos.x);
		buffer.writeVarInt(newPos.z);
		return new ClientboundSetChunkCacheCenterPacket(buffer);
	}

	/**
	private static ClientboundDamageEventPacket transformPacket(ClientboundDamageEventPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
	}**/

	/**
	private static ClientboundChunksBiomesPacket transformPacket(ClientboundChunksBiomesPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
	}**/


	 private static ClientboundSoundPacket transformPacket(ClientboundSoundPacket packet, ServerPlayer player) {
		 FriendlyByteBuf buffer = PacketByteBufs.create();
		 buffer.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), packet.getSound(), (friendlyByteBuf, soundEvent) -> soundEvent.writeToNetwork(friendlyByteBuf));
		 buffer.writeEnum(packet.getSource());
		 buffer.writeInt((int) (getClientX(player, packet.getX()) * 8.0));
		 buffer.writeInt((int) (packet.getY() * 8.0));
		 buffer.writeInt((int) (getClientZ(player, packet.getZ()) * 8.0));
		 buffer.writeFloat(packet.getVolume());
		 buffer.writeFloat(packet.getPitch());
		 buffer.writeLong(packet.getSeed());

		 return new ClientboundSoundPacket(buffer);
	 }


	private static ClientboundExplodePacket transformPacket(ClientboundExplodePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();

		WorldTransformer transformer = player.serverLevel().getTransformer();

		double clientWrappedX = getClientX(player, packet.getX());
		double clientWrappedZ = getClientZ(player, packet.getZ());

		buffer.writeDouble(clientWrappedX);
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(clientWrappedZ);
		buffer.writeFloat(packet.getPower());
		int i = Mth.floor(clientWrappedX);
		int j = Mth.floor(packet.getY());
		int k = Mth.floor(clientWrappedZ);
		buffer.writeCollection(packet.getToBlow(), (friendlyByteBuf, blockPos) -> {
			int newX = getClientX(player, blockPos.getX());
			int newZ = getClientZ(player, blockPos.getZ());

			int l = newX - i;
			int m = blockPos.getY() - j;
			int n = newZ - k;
			friendlyByteBuf.writeByte(l);
			friendlyByteBuf.writeByte(m);
			friendlyByteBuf.writeByte(n);
		});
		buffer.writeFloat(packet.getKnockbackX());
		buffer.writeFloat(packet.getKnockbackY());
		buffer.writeFloat(packet.getKnockbackZ());
		buffer.writeEnum(packet.getBlockInteraction());
		packet.writeParticle(buffer, packet.getSmallExplosionParticles());
		packet.writeParticle(buffer, packet.getLargeExplosionParticles());
		packet.getExplosionSound().writeToNetwork(buffer);

		return new ClientboundExplodePacket(buffer);
	}

	private static ClientboundLevelParticlesPacket transformPacket(ClientboundLevelParticlesPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();

		buffer.writeId(BuiltInRegistries.PARTICLE_TYPE, packet.getParticle().getType());
		buffer.writeBoolean(packet.isOverrideLimiter());
		buffer.writeDouble(getClientX(player, packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(getClientZ(player, packet.getZ()));
		buffer.writeFloat(packet.getXDist());
		buffer.writeFloat(packet.getYDist());
		buffer.writeFloat(packet.getZDist());
		buffer.writeFloat(packet.getMaxSpeed());
		buffer.writeInt(packet.getCount());
		packet.getParticle().writeToNetwork(buffer);

		return new ClientboundLevelParticlesPacket(buffer);
	}

	private static ClientboundOpenSignEditorPacket transformPacket(ClientboundOpenSignEditorPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
		buffer.writeBoolean(packet.isFrontText());
		return new ClientboundOpenSignEditorPacket(buffer);
	}


	private static ClientboundBlockEventPacket transformPacket(ClientboundBlockEventPacket packet, ServerPlayer player) {
    FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
		buffer.writeByte(packet.getB0());
		buffer.writeByte(packet.getB1());
		buffer.writeId(BuiltInRegistries.BLOCK, packet.getBlock());
		return new ClientboundBlockEventPacket(buffer);
	}

	private static ClientboundForgetLevelChunkPacket transformPacket(ClientboundForgetLevelChunkPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeChunkPos(getClientChunkPos(player, packet.pos()));
		return new ClientboundForgetLevelChunkPacket(buffer);
	}

	private static ClientboundBlockUpdatePacket transformPacket(ClientboundBlockUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
		buffer.writeId(Block.BLOCK_STATE_REGISTRY, packet.getBlockState());
		return new ClientboundBlockUpdatePacket(buffer);
	}
  
	private static ClientboundBlockDestructionPacket transformPacket(ClientboundBlockDestructionPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeVarInt(packet.getId());
		buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
		buffer.writeByte(packet.getProgress());

		return new ClientboundBlockDestructionPacket(buffer);
	}

	private static ClientboundSectionBlocksUpdatePacket transformPacket(ClientboundSectionBlocksUpdatePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();

		//Translate the SectionPos relative to the player
		SectionPos originalSectionPos = packet.sectionPos;
		ChunkPos playerChunk = getClientChunkPos(player, originalSectionPos.chunk());
		SectionPos newPos = SectionPos.of(playerChunk, originalSectionPos.y());

		buffer.writeLong(newPos.asLong());
		buffer.writeVarInt(packet.positions.length);

		for (int i = 0; i < packet.positions.length; i++) {
			buffer.writeVarLong((long)Block.getId(packet.states[i]) << 12 | (long)packet.positions[i]);
		}

		return new ClientboundSectionBlocksUpdatePacket(buffer);
	}

	private static ClientboundAddExperienceOrbPacket transformPacket(ClientboundAddExperienceOrbPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeVarInt(packet.getId());
		buffer.writeDouble(getClientX(player, packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(getClientZ(player, packet.getZ()));
		buffer.writeShort(packet.getValue());

		return new ClientboundAddExperienceOrbPacket(buffer);
	}

	/**
	private static ClientboundPlayerLookAtPacket transformPacket(ClientboundPlayerLookAtPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
	}**/

	private static ClientboundLevelEventPacket transformPacket(ClientboundLevelEventPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeInt(packet.getType());
		buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
		buffer.writeInt(packet.getData());
		buffer.writeBoolean(packet.isGlobalEvent());

		return new ClientboundLevelEventPacket(buffer);
	}



	private static ClientboundPlayerPositionPacket transformPacket(ClientboundPlayerPositionPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeDouble(getClientX(player, packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(getClientZ(player, packet.getZ()));
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
		buffer.writeDouble(getClientX(player, packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(getClientZ(player, packet.getZ()));
		buffer.writeByte((int) ((packet.getXRot() * 256.0) / 360));
		buffer.writeByte((int) ((packet.getYRot() * 256.0) / 360));
		buffer.writeByte((int) ((packet.getYHeadRot() * 256.0) / 360));
		buffer.writeVarInt(packet.getData());
		buffer.writeShort((int) packet.getXa() / 8000);
		buffer.writeShort((int) packet.getYa() / 8000);
		buffer.writeShort((int) packet.getZa() / 8000);
		return new ClientboundAddEntityPacket(buffer);
	}
  
	private static ClientboundMoveVehiclePacket transformPacket(ClientboundMoveVehiclePacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeDouble(getClientX(player, packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(getClientZ(player, packet.getZ()));
		buffer.writeFloat(packet.getYRot());
		buffer.writeFloat(packet.getXRot());

		return new ClientboundMoveVehiclePacket(buffer);
	}

	private static ClientboundTeleportEntityPacket transformPacket(ClientboundTeleportEntityPacket packet, ServerPlayer player) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeVarInt(packet.getId());
		buffer.writeDouble(getClientX(player, packet.getX()));
		buffer.writeDouble(packet.getY());
		buffer.writeDouble(getClientZ(player, packet.getZ()));
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
