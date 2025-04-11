/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
	public static <T extends PacketListener> Packet<T> process(Packet<T> packet, ServerPlayer player){
		Method transformedPacket = methodCache.get(packet.getClass());

		//A method does not exist to handle the packet. Don't transform it
		if(transformedPacket == null) {
			return packet;
		}
		try {
			//Process the packet with its transformer method. TODO find a way to automatically register all the overrided transformPacket methods without the use of reflection. Reflection invoking is slow.
			return (Packet<T>) transformedPacket.invoke(PacketTransformer.class, packet, player);
		//This should never occur.
		} catch (InvocationTargetException | IllegalAccessException e) {
			LOGGER.error("{} couldn't be processed by the transformer!", packet.getClass().getSimpleName());
			return packet;
		}
	}

	@FunctionalInterface
	private interface BufferFiller<T> {
		void fill(T buffer);
	}

	private static <B extends ByteBuf, T> T newBuffer(StreamCodec<B, T> codec, BufferFiller<B> filler) {
		B buffer = (B) new RegistryFriendlyByteBuf(PacketByteBufs.create(), TransformerRequests.server.registryAccess());
		filler.fill(buffer);
		return codec.decode(buffer);
	}

	private static DimensionTransformer playerTransformer(ServerPlayer serverPlayer) {
		return serverPlayer.serverLevel().getTransformer();
	}

	private static double getClientX(ServerPlayer player, double packetX) {
		return playerTransformer(player).Coord.X.unwrapFromBounds(player.getClientX(), packetX);
	}

	private static double getServerX(ServerPlayer player, double packetX) {
		return playerTransformer(player).Coord.X.wrapToBounds(packetX);
	}

	private static double getClientZ(ServerPlayer player, double packetZ) {
		return playerTransformer(player).Coord.Z.unwrapFromBounds(player.getClientZ(), packetZ);
	}

	private static double getServerZ(ServerPlayer player, double packetZ) {
		return playerTransformer(player).Coord.Z.wrapToBounds(packetZ);
	}

	private static int getClientX(ServerPlayer player, int packetX) {
		return playerTransformer(player).Coord.X.unwrapFromBounds(player.getClientBlock().getX(), packetX);
	}

	private static int getServerX(ServerPlayer player, int packetX) {
		return playerTransformer(player).Coord.X.wrapToBounds(packetX);
	}

	private static int getClientZ(ServerPlayer player, int packetZ) {
		return playerTransformer(player).Coord.Z.unwrapFromBounds(player.getClientBlock().getZ(), packetZ);
	}

	private static int getServerZ(ServerPlayer player, int packetZ) {
		return playerTransformer(player).Coord.Z.wrapToBounds(packetZ);
	}

	private static ChunkPos getClientChunkPos(ServerPlayer player, ChunkPos packetChunkPos) {
		return playerTransformer(player).Chunk.unwrapFromBounds(player.getClientChunk(), packetChunkPos);
	}

	private static BlockPos getClientBlockPos(ServerPlayer player, BlockPos packetBlockPos) {
		return playerTransformer(player).Block.unwrapFromBounds(player.getClientBlock(), packetBlockPos);
	}

	private static BlockPos getServerBlockPos(ServerPlayer player, BlockPos packetBlockPos) {
		return playerTransformer(player).Block.wrapToBounds(packetBlockPos);
	}

	private static Vec3 getClientVec3(ServerPlayer player, Vec3 packetVec3) {
		return playerTransformer(player).Vector3D.unwrapFromBounds(player.getClientPosition(), packetVec3);
	}

	private static Vec3 getServerVec3(ServerPlayer player, Vec3 packetVec3) {
		return playerTransformer(player).Vector3D.wrapToBounds(packetVec3);
	}

	private static AABB getClientAABB(ServerPlayer player, AABB packetAABB) {
		return playerTransformer(player).AABoundingBox.unwrapFromBounds(new AABB(player.getClientBlock()), packetAABB);
	}

	private static BoundingBox getClientBB(ServerPlayer player, BoundingBox packetBB) {
		return playerTransformer(player).BoundingBoxes.unwrapFromBounds(new AABB(player.getClientBlock()), AABB.of(packetBB));
	}

	private static int getLimitedDistance(ServerPlayer player, int distance) {
		return playerTransformer(player).limitViewDistance(distance);
	}

	private static ServerboundPlayerActionPacket transformPacket(ServerboundPlayerActionPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundPlayerActionPacket.STREAM_CODEC, buffer -> {
			buffer.writeEnum(packet.getAction());
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
			buffer.writeByte(packet.getDirection().get3DDataValue());
			buffer.writeVarInt(packet.getSequence());
		});
	}

	private static ServerboundBlockEntityTagQueryPacket transformPacket(ServerboundBlockEntityTagQueryPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundBlockEntityTagQueryPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.getTransactionId());
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
		});
	}

	private static ServerboundInteractPacket transformPacket(ServerboundInteractPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundInteractPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.getTarget(player.serverLevel()).getId());
			buffer.writeEnum(packet.action.getType());

			if(packet.action instanceof ServerboundInteractPacket.InteractionAtLocationAction locationAction) {
				buffer.writeFloat((float)getServerX(player, locationAction.location.x));
				buffer.writeFloat((float)locationAction.location.y);
				buffer.writeFloat((float)getServerZ(player, locationAction.location.z));
				buffer.writeEnum(locationAction.hand);
			}
			else packet.action.write(buffer);

			buffer.writeBoolean(packet.isUsingSecondaryAction());
		});
	}

	private static ServerboundMovePlayerPacket.Pos transformPacket(ServerboundMovePlayerPacket.Pos packet, ServerPlayer player) {
		player.setClientX(packet.getX(0));
		player.setClientZ(packet.getZ(0));

		return newBuffer(ServerboundMovePlayerPacket.Pos.STREAM_CODEC, buffer -> {
			buffer.writeDouble(getServerX(player, packet.getX(0)));
			buffer.writeDouble(packet.getY(0));
			buffer.writeDouble(getServerZ(player, packet.getZ(0)));
			buffer.writeByte(packet.isOnGround() ? 1 : 0);
		});
	}

	private static ServerboundMovePlayerPacket.PosRot transformPacket(ServerboundMovePlayerPacket.PosRot packet, ServerPlayer player) {
		player.setClientX(packet.getX(0));
		player.setClientZ(packet.getZ(0));

		return newBuffer(ServerboundMovePlayerPacket.PosRot.STREAM_CODEC, buffer -> {
			buffer.writeDouble(getServerX(player, packet.getX(0)));
			buffer.writeDouble(packet.getY(0));
			buffer.writeDouble(getServerZ(player, packet.getZ(0)));
			buffer.writeFloat(packet.getYRot(0));
			buffer.writeFloat(packet.getXRot(0));
			buffer.writeByte(packet.isOnGround() ? 1 : 0);
		});
	}

	private static ServerboundMoveVehiclePacket transformPacket(ServerboundMoveVehiclePacket packet, ServerPlayer player) {
		return newBuffer(ServerboundMoveVehiclePacket.STREAM_CODEC, buffer -> {
			buffer.writeDouble(getServerX(player, packet.getX()));
			buffer.writeDouble(packet.getY());
			buffer.writeDouble(getServerZ(player, packet.getZ()));
			buffer.writeFloat(packet.getYRot());
			buffer.writeFloat(packet.getXRot());
		});
	}

	private static ServerboundJigsawGeneratePacket transformPacket(ServerboundJigsawGeneratePacket packet, ServerPlayer player) {
		return newBuffer(ServerboundJigsawGeneratePacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
			buffer.writeVarInt(packet.levels());
			buffer.writeBoolean(packet.keepJigsaws());
		});
	}

	private static ServerboundSetCommandBlockPacket transformPacket(ServerboundSetCommandBlockPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundSetCommandBlockPacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
			buffer.writeUtf(packet.getCommand());
			buffer.writeEnum(packet.getMode());
			int i = 0;
			if (packet.isTrackOutput()) {
				i |= 1;
			}

			if (packet.isConditional()) {
				i |= 2;
			}

			if (packet.isAutomatic()) {
				i |= 4;
			}

			buffer.writeByte(i);
		});
	}

	private static ServerboundSetJigsawBlockPacket transformPacket(ServerboundSetJigsawBlockPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundSetJigsawBlockPacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
			buffer.writeResourceLocation(packet.getName());
			buffer.writeResourceLocation(packet.getTarget());
			buffer.writeResourceLocation(packet.getPool());
			buffer.writeUtf(packet.getFinalState());
			buffer.writeUtf(packet.getJoint().getSerializedName());
			buffer.writeVarInt(packet.getSelectionPriority());
			buffer.writeVarInt(packet.getPlacementPriority());
		});
	}

	private static ServerboundSetStructureBlockPacket transformPacket(ServerboundSetStructureBlockPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundSetStructureBlockPacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
			buffer.writeEnum(packet.getUpdateType());
			buffer.writeEnum(packet.getMode());
			buffer.writeUtf(packet.getName());
			buffer.writeByte(packet.getOffset().getX());
			buffer.writeByte(packet.getOffset().getY());
			buffer.writeByte(packet.getOffset().getZ());
			buffer.writeByte(packet.getSize().getX());
			buffer.writeByte(packet.getSize().getY());
			buffer.writeByte(packet.getSize().getZ());
			buffer.writeEnum(packet.getMirror());
			buffer.writeEnum(packet.getRotation());
			buffer.writeUtf(packet.getData());
			buffer.writeFloat(packet.getIntegrity());
			buffer.writeVarLong(packet.getSeed());
			int i = 0;
			if (packet.isIgnoreEntities()) {
				i |= 1;
			}

			if (packet.isShowAir()) {
				i |= 2;
			}

			if (packet.isShowBoundingBox()) {
				i |= 4;
			}

			buffer.writeByte(i);
		});
	}

	private static ServerboundSignUpdatePacket transformPacket(ServerboundSignUpdatePacket packet, ServerPlayer player) {
		return newBuffer(ServerboundSignUpdatePacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getServerBlockPos(player, packet.getPos()));
			buffer.writeBoolean(packet.isFrontText());

			for (int i = 0; i < 4; i++) {
				buffer.writeUtf(packet.getLines()[i]);
			}
		});
	}

	private static ServerboundUseItemOnPacket transformPacket(ServerboundUseItemOnPacket packet, ServerPlayer player) {
		return newBuffer(ServerboundUseItemOnPacket.STREAM_CODEC, buffer -> {
			buffer.writeEnum(packet.getHand());

			DimensionTransformer transformer = playerTransformer(player);
			BlockHitResult oldBlockHit = packet.getHitResult();
			BlockHitResult newBlockHit = new BlockHitResult(getServerVec3(player, oldBlockHit.getLocation()), oldBlockHit.getDirection(), getServerBlockPos(player, oldBlockHit.getBlockPos()), oldBlockHit.isInside());
			buffer.writeBlockHitResult(newBlockHit);

			buffer.writeVarInt(packet.getSequence());
		});
	}

	private static ClientboundLoginPacket transformPacket(ClientboundLoginPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundLoginPacket.STREAM_CODEC, buffer -> {
			buffer.writeInt(packet.playerId());
			buffer.writeBoolean(packet.hardcore());
			buffer.writeCollection(packet.levels(), FriendlyByteBuf::writeResourceKey);
			buffer.writeVarInt(packet.maxPlayers());
			buffer.writeVarInt(getLimitedDistance(player, packet.chunkRadius()));
			buffer.writeVarInt(getLimitedDistance(player, packet.simulationDistance()));
			buffer.writeBoolean(packet.reducedDebugInfo());
			buffer.writeBoolean(packet.showDeathScreen());
			buffer.writeBoolean(packet.doLimitedCrafting());
			packet.commonPlayerSpawnInfo().write(buffer);
			buffer.writeBoolean(packet.enforcesSecureChat());
		});
	}

	private static ClientboundSetChunkCacheRadiusPacket transformPacket(ClientboundSetChunkCacheRadiusPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundSetChunkCacheRadiusPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(getLimitedDistance(player, packet.getRadius()));
		});
	}

	private static ClientboundSetSimulationDistancePacket transformPacket(ClientboundSetSimulationDistancePacket packet, ServerPlayer player) {
		return newBuffer(ClientboundSetSimulationDistancePacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(getLimitedDistance(player, packet.simulationDistance()));
		});
	}

	private static ClientboundSetChunkCacheCenterPacket transformPacket(ClientboundSetChunkCacheCenterPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundSetChunkCacheCenterPacket.STREAM_CODEC, buffer -> {
			ChunkPos newPos = getClientChunkPos(player, new ChunkPos(packet.getX(), packet.getZ()));
			buffer.writeVarInt(newPos.x);
			buffer.writeVarInt(newPos.z);
		});
	}

	private static ClientboundLightUpdatePacket transformPacket(ClientboundLightUpdatePacket packet, ServerPlayer player) {
		return newBuffer(ClientboundLightUpdatePacket.STREAM_CODEC, buffer -> {
			ChunkPos newPos = getClientChunkPos(player, new ChunkPos(packet.getX(), packet.getZ()));
			buffer.writeVarInt(newPos.x);
			buffer.writeVarInt(newPos.z);
			packet.getLightData().write(buffer);
		});
	}

	private static ClientboundLevelChunkWithLightPacket transformPacket(ClientboundLevelChunkWithLightPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundLevelChunkWithLightPacket.STREAM_CODEC, buffer -> {
			ChunkPos newPos = getClientChunkPos(player, new ChunkPos(packet.getX(), packet.getZ()));
			buffer.writeInt(newPos.x);
			buffer.writeInt(newPos.z);
			packet.getChunkData().write(buffer);
			packet.getLightData().write(buffer);
		});
	}

	private static ClientboundDamageEventPacket transformPacket(ClientboundDamageEventPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundDamageEventPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.entityId());
			DamageType.STREAM_CODEC.encode(buffer, packet.sourceType());
			buffer.writeVarInt(packet.sourceCauseId() + 1);
			buffer.writeVarInt(packet.sourceDirectId() + 1);

			Optional<Vec3> oldPos = packet.sourcePosition();
			Optional<Vec3> newPos = Optional.empty();
			if(oldPos.isPresent()) {
				newPos = Optional.of(getClientVec3(player, oldPos.get()));
			}
			buffer.writeOptional(newPos, (friendlyByteBuf, vec3) -> {
				friendlyByteBuf.writeDouble(vec3.x());
				friendlyByteBuf.writeDouble(vec3.y());
				friendlyByteBuf.writeDouble(vec3.z());
			});
		});
	}

	private static ClientboundChunksBiomesPacket transformPacket(ClientboundChunksBiomesPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundChunksBiomesPacket.STREAM_CODEC, buffer -> {
			List<ClientboundChunksBiomesPacket.ChunkBiomeData> newData = new ArrayList<>();

			packet.chunkBiomeData().stream().toList().forEach(chunkBiomeData -> {
				newData.add(new ClientboundChunksBiomesPacket.ChunkBiomeData(getClientChunkPos(player, chunkBiomeData.pos()), chunkBiomeData.buffer()));
			});

			buffer.writeCollection(newData, (friendlyByteBuf, chunkBiomeData) -> chunkBiomeData.write(friendlyByteBuf));
		});
	}

	 private static ClientboundSoundPacket transformPacket(ClientboundSoundPacket packet, ServerPlayer player) {
		 return newBuffer(ClientboundSoundPacket.STREAM_CODEC, buffer -> {
			 SoundEvent.STREAM_CODEC.encode(buffer, packet.getSound());
			 buffer.writeEnum(packet.getSource());
			 buffer.writeInt((int) (getClientX(player, packet.getX()) * 8.0));
			 buffer.writeInt((int) (packet.getY() * 8.0));
			 buffer.writeInt((int) (getClientZ(player, packet.getZ()) * 8.0));
			 buffer.writeFloat(packet.getVolume());
			 buffer.writeFloat(packet.getPitch());
			 buffer.writeLong(packet.getSeed());
		 });
	 }


	private static ClientboundExplodePacket transformPacket(ClientboundExplodePacket packet, ServerPlayer player) {
		return newBuffer(ClientboundExplodePacket.STREAM_CODEC, buffer -> {
			DimensionTransformer transformer = player.serverLevel().getTransformer();

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
			ParticleTypes.STREAM_CODEC.encode(buffer, packet.getSmallExplosionParticles());
			ParticleTypes.STREAM_CODEC.encode(buffer, packet.getLargeExplosionParticles());
			SoundEvent.STREAM_CODEC.encode(buffer, packet.getExplosionSound());
		});
	}

	private static ClientboundLevelParticlesPacket transformPacket(ClientboundLevelParticlesPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundLevelParticlesPacket.STREAM_CODEC, buffer -> {
			buffer.writeBoolean(packet.isOverrideLimiter());
			buffer.writeDouble(getClientX(player, packet.getX()));
			buffer.writeDouble(packet.getY());
			buffer.writeDouble(getClientZ(player, packet.getZ()));
			buffer.writeFloat(packet.getXDist());
			buffer.writeFloat(packet.getYDist());
			buffer.writeFloat(packet.getZDist());
			buffer.writeFloat(packet.getMaxSpeed());
			buffer.writeInt(packet.getCount());

			ParticleOptions options = packet.getParticle();

			//This particle is the only known one which uses a position.
			if(options instanceof VibrationParticleOption particleOption) {
				Optional<Vec3> position = particleOption.getDestination().getPosition(player.serverLevel());
				Optional<Vec3> newPosition = Optional.empty();
				if(position.isPresent()) newPosition = Optional.of(getClientVec3(player, position.get()));
				if(newPosition.isPresent()) options = new VibrationParticleOption(new BlockPositionSource(new BlockPos(Mth.floor(newPosition.get().x), Mth.floor(newPosition.get().y), Mth.floor(newPosition.get().z))), particleOption.getArrivalInTicks());
			}

			ParticleTypes.STREAM_CODEC.encode(buffer, options);
		});
	}

	private static ClientboundOpenSignEditorPacket transformPacket(ClientboundOpenSignEditorPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundOpenSignEditorPacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
			buffer.writeBoolean(packet.isFrontText());
		});
	}


	private static ClientboundBlockEventPacket transformPacket(ClientboundBlockEventPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundBlockEventPacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
			buffer.writeByte(packet.getB0());
			buffer.writeByte(packet.getB1());
			ByteBufCodecs.registry(Registries.BLOCK).encode(buffer, packet.getBlock());
		});

	}

	private static ClientboundForgetLevelChunkPacket transformPacket(ClientboundForgetLevelChunkPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundForgetLevelChunkPacket.STREAM_CODEC, buffer -> {
			buffer.writeChunkPos(getClientChunkPos(player, packet.pos()));
		});
	}

	private static ClientboundBlockUpdatePacket transformPacket(ClientboundBlockUpdatePacket packet, ServerPlayer player) {
		return newBuffer(ClientboundBlockUpdatePacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
			ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).encode(buffer, packet.getBlockState());
		});
	}

	private static ClientboundBlockDestructionPacket transformPacket(ClientboundBlockDestructionPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundBlockDestructionPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.getId());
			buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
			buffer.writeByte(packet.getProgress());
		});
	}

	private static ClientboundSectionBlocksUpdatePacket transformPacket(ClientboundSectionBlocksUpdatePacket packet, ServerPlayer player) {
		return newBuffer(ClientboundSectionBlocksUpdatePacket.STREAM_CODEC, buffer -> {
			//Translate the SectionPos relative to the player
			SectionPos originalSectionPos = packet.sectionPos;
			ChunkPos playerChunk = getClientChunkPos(player, originalSectionPos.chunk());
			SectionPos newPos = SectionPos.of(playerChunk, originalSectionPos.y());

			buffer.writeLong(newPos.asLong());
			buffer.writeVarInt(packet.positions.length);

			for (int i = 0; i < packet.positions.length; i++) {
				buffer.writeVarLong((long)Block.getId(packet.states[i]) << 12 | (long)packet.positions[i]);
			}
		});
	}

	private static ClientboundAddExperienceOrbPacket transformPacket(ClientboundAddExperienceOrbPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundAddExperienceOrbPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.getId());
			buffer.writeDouble(getClientX(player, packet.getX()));
			buffer.writeDouble(packet.getY());
			buffer.writeDouble(getClientZ(player, packet.getZ()));
			buffer.writeShort(packet.getValue());
		});
	}


	private static ClientboundPlayerLookAtPacket transformPacket(ClientboundPlayerLookAtPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundPlayerLookAtPacket.STREAM_CODEC, buffer -> {
			buffer.writeEnum(packet.getFromAnchor());
			buffer.writeDouble(getClientX(player, packet.x));
			buffer.writeDouble(packet.y);
			buffer.writeDouble(getClientZ(player, packet.z));
			buffer.writeBoolean(packet.atEntity);
			if (packet.atEntity) {
				buffer.writeVarInt(packet.entity);
				buffer.writeEnum(packet.toAnchor);
			}
		});
	}

	private static ClientboundLevelEventPacket transformPacket(ClientboundLevelEventPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundLevelEventPacket.STREAM_CODEC, buffer -> {
			buffer.writeInt(packet.getType());
			buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
			buffer.writeInt(packet.getData());
			buffer.writeBoolean(packet.isGlobalEvent());
		});
	}



	private static ClientboundPlayerPositionPacket transformPacket(ClientboundPlayerPositionPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundPlayerPositionPacket.STREAM_CODEC, buffer -> {
			if(packet.getRelativeArguments().contains(RelativeMovement.X)) buffer.writeDouble(packet.getX());
			else buffer.writeDouble(getClientX(player, packet.getX()));

			buffer.writeDouble(packet.getY());

			if(packet.getRelativeArguments().contains(RelativeMovement.Z)) buffer.writeDouble(packet.getZ());
			else buffer.writeDouble(getClientZ(player, packet.getZ()));

			buffer.writeFloat(packet.getYRot());
			buffer.writeFloat(packet.getXRot());
			buffer.writeByte(RelativeMovement.pack(packet.getRelativeArguments()));
			buffer.writeVarInt(packet.getId());
		});
	}

	private static ClientboundBlockEntityDataPacket transformPacket(ClientboundBlockEntityDataPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundBlockEntityDataPacket.STREAM_CODEC, buffer -> {
			buffer.writeBlockPos(getClientBlockPos(player, packet.getPos()));
			ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).encode(buffer, packet.getType());
			ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buffer, packet.getTag());
		});
	}

	private static ClientboundSetEntityDataPacket transformPacket(ClientboundSetEntityDataPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundSetEntityDataPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.id());

			List<SynchedEntityData.DataValue<?>> repackedItems = new ArrayList<>();

			for (SynchedEntityData.DataValue<?> dataValue : packet.packedItems()) {
				if(dataValue.serializer().equals(EntityDataSerializers.BLOCK_POS)) {
					BlockPos newBlockPos = getClientBlockPos(player, (BlockPos) dataValue.value());
					SynchedEntityData.DataValue<BlockPos> newValue = new SynchedEntityData.DataValue<>(dataValue.id(), EntityDataSerializers.BLOCK_POS, newBlockPos);
					repackedItems.add(newValue);
				}
				else if(dataValue.serializer().equals(EntityDataSerializers.OPTIONAL_BLOCK_POS)) {
					Optional<BlockPos> value = (Optional<BlockPos>) dataValue.value();
					Optional<BlockPos> newBlockPos = Optional.empty();
					if(value.isPresent()) newBlockPos = Optional.of(getClientBlockPos(player, value.get()));

					SynchedEntityData.DataValue<Optional<BlockPos>> newValue = new SynchedEntityData.DataValue<>(dataValue.id(), EntityDataSerializers.OPTIONAL_BLOCK_POS, newBlockPos);
					repackedItems.add(newValue);
				}
				else if(dataValue.serializer().equals(EntityDataSerializers.OPTIONAL_GLOBAL_POS)) {
					Optional<GlobalPos> value = (Optional<GlobalPos>) dataValue.value();
					Optional<GlobalPos> newGlobalPos = Optional.empty();
					if(value.isPresent()) newGlobalPos = Optional.of(new GlobalPos(value.get().dimension(), getClientBlockPos(player, value.get().pos())));

					SynchedEntityData.DataValue<Optional<GlobalPos>> newValue = new SynchedEntityData.DataValue<>(dataValue.id(), EntityDataSerializers.OPTIONAL_GLOBAL_POS, newGlobalPos);
					repackedItems.add(newValue);
				}
				else if(dataValue.serializer().equals(EntityDataSerializers.VECTOR3)) {
					Vector3f value = (Vector3f) dataValue.value();
					Vector3f newVector3f = new Vector3f((float) getClientX(player, value.x), value.y, (float) getClientZ(player, value.z));

					SynchedEntityData.DataValue<Vector3f> newValue = new SynchedEntityData.DataValue<>(dataValue.id(), EntityDataSerializers.VECTOR3, newVector3f);
					repackedItems.add(newValue);
				}
				else {
					repackedItems.add(dataValue);
				}
			}

			for (SynchedEntityData.DataValue<?> dataValue : repackedItems) {
				dataValue.write(buffer);
			}

			buffer.writeByte(255);
		});
	}

	private static ClientboundAddEntityPacket transformPacket(ClientboundAddEntityPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundAddEntityPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.getId());
			buffer.writeUUID(packet.getUUID());
			ByteBufCodecs.registry(Registries.ENTITY_TYPE).encode(buffer, packet.getType());
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
		});
	}

	private static ClientboundMoveVehiclePacket transformPacket(ClientboundMoveVehiclePacket packet, ServerPlayer player) {
		return newBuffer(ClientboundMoveVehiclePacket.STREAM_CODEC, buffer -> {
			buffer.writeDouble(getClientX(player, packet.getX()));
			buffer.writeDouble(packet.getY());
			buffer.writeDouble(getClientZ(player, packet.getZ()));
			buffer.writeFloat(packet.getYRot());
			buffer.writeFloat(packet.getXRot());
		});
	}

	private static ClientboundTeleportEntityPacket transformPacket(ClientboundTeleportEntityPacket packet, ServerPlayer player) {
		return newBuffer(ClientboundTeleportEntityPacket.STREAM_CODEC, buffer -> {
			buffer.writeVarInt(packet.getId());
			buffer.writeDouble(getClientX(player, packet.getX()));
			buffer.writeDouble(packet.getY());
			buffer.writeDouble(getClientZ(player, packet.getZ()));
			buffer.writeByte(packet.getyRot());
			buffer.writeByte(packet.getxRot());
			buffer.writeBoolean(packet.isOnGround());
		});
	}

	private static ClientboundBundlePacket transformPacket(ClientboundBundlePacket packet, ServerPlayer player) {
		List<Packet<? super ClientGamePacketListener>> outputPackets = new ArrayList<>();
		packet.subPackets().forEach((subPacket) -> {
			outputPackets.add(PacketTransformer.process(subPacket, player));
		});

		return new ClientboundBundlePacket(outputPackets);
	}

	private static ClientboundCustomPayloadPacket transformPacket(ClientboundCustomPayloadPacket packet, ServerPlayer player) {
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(PacketByteBufs.create(), player.getServer().registryAccess());

		CustomPacketPayload outputPayload = packet.payload();

		if(packet.payload() instanceof NeighborUpdatesDebugPayload payload) {
			outputPayload = new NeighborUpdatesDebugPayload(payload.time(), getClientBlockPos(player, payload.pos()));
		}
		else if(packet.payload() instanceof PathfindingDebugPayload payload) {
			outputPayload = pathfindingDebugPayload(payload, player);
		}

		return new ClientboundCustomPayloadPacket(outputPayload);
	}

	private static PathfindingDebugPayload pathfindingDebugPayload(PathfindingDebugPayload payload, ServerPlayer player) {
		Path inputPath = payload.path();

		List<Node> outputNodes = new ArrayList<>();

		//Wrap node positions
		for(int i = 0; i < inputPath.getNodeCount(); i++) {
			Node inputNode = inputPath.getNode(i);
			outputNodes.add(wrapNode(player, inputNode));
		}

		Path outputPath = new Path(outputNodes, getClientBlockPos(player, inputPath.getTarget()), inputPath.canReach());

		//Wrap debugData
		if(inputPath.debugData() != null) {
			Node[] inputOpenNodes = inputPath.debugData().openSet();
			Node[] outputOpenNodes = new Node[inputOpenNodes.length];

			for(int i = 0; i < inputOpenNodes.length; i++) {
				outputOpenNodes[i] = wrapNode(player, inputOpenNodes[i]);
			}

			Node[] inputClosedNodes = inputPath.debugData().closedSet();
			Node[] outputClosedNodes = new Node[inputClosedNodes.length];

			for(int i = 0; i < inputClosedNodes.length; i++) {
				outputClosedNodes[i] = wrapNode(player, inputClosedNodes[i]);
			}


			Set<Target> inputTargets = inputPath.debugData().targetNodes();
			Set<Target> outputTargets = new HashSet<>();

			 inputTargets.forEach(target -> {
			 Target newTarget = new Target(wrapNode(player, target));
			 newTarget.updateBest(target.bestHeuristic, target.getBestNode());
			 newTarget.reached = target.isReached();
			 outputTargets.add(newTarget);
			 });

			outputPath.setDebug(outputOpenNodes, outputClosedNodes, inputTargets);
		}

		return new PathfindingDebugPayload(payload.entityId(), outputPath, payload.maxNodeDistance());
	}

	private static Node wrapNode(ServerPlayer player, Node node) {

		RegistryFriendlyByteBuf nodeBuffer = new RegistryFriendlyByteBuf(PacketByteBufs.create(), player.getServer().registryAccess());
		nodeBuffer.writeInt(getClientX(player, node.x));
		nodeBuffer.writeInt(node.y);
		nodeBuffer.writeInt(getClientZ(player, node.z));
		nodeBuffer.writeFloat(node.walkedDistance);
		nodeBuffer.writeFloat(node.costMalus);
		nodeBuffer.writeBoolean(node.closed);
		nodeBuffer.writeEnum(node.type);
		nodeBuffer.writeFloat(node.f);

		return Node.createFromStream(nodeBuffer);
	}
}
