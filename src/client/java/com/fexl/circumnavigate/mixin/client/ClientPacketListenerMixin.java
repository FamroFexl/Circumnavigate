/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.client;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	ClientPacketListener thiz = (ClientPacketListener) (Object) this;

	ClientPacketListenerAccessorMixin accessor = (ClientPacketListenerAccessorMixin) (Object) thiz;

	@Inject(method = "handleChunkBlocksUpdate", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		packet.runUpdates((blockPos, blockState) -> thiz.level.setServerVerifiedBlockState((BlockPos)transformer.translateBlockFromBounds(Minecraft.getInstance().player.blockPosition(), blockPos), (BlockState)blockState, 19));
	}

	@Inject(method = "handleLevelChunkWithLight", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet) packet, (PacketListener) thiz, thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		//This check should have been done by the server, but in the case it wasn't, this check ensures no bad chunks are used.
		if(packet.getX() > transformer.xChunkBoundMax - 1 || packet.getX() < transformer.xChunkBoundMin || packet.getZ() > transformer.zChunkBoundMax - 1 || packet.getZ() < transformer.zChunkBoundMin) {
			return;
		}

		int i = transformer.xTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().x, packet.getX());
		int j = transformer.zTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().z, packet.getZ());

		accessor.updateLevelChunkAM(i, j, packet.getChunkData());
		ClientboundLightUpdatePacketData clientboundLightUpdatePacketData = packet.getLightData();
		accessor.getLevel().queueLightUpdate(() -> {
			accessor.applyLightDataAM(i, j, clientboundLightUpdatePacketData);
			LevelChunk levelChunk = accessor.getLevel().getChunkSource().getChunk(i, j, false);
			if (levelChunk != null) {
				accessor.enableChunkLightAM(levelChunk, i, j);
			}
		});
	}

	@Inject(method = "handleChunksBiomes", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleChunksBiomes(ClientboundChunksBiomesPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		for (ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData : packet.chunkBiomeData()) {
			thiz.level.getChunkSource().replaceBiomes(transformer.xTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().x, chunkBiomeData.pos().x), transformer.zTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().z, chunkBiomeData.pos().z), chunkBiomeData.getReadBuffer());
		}
		for (ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData : packet.chunkBiomeData()) {
			thiz.level.onChunkLoaded(new ChunkPos(transformer.xTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().x, chunkBiomeData.pos().x), transformer.zTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().z, chunkBiomeData.pos().z)));
		}
		for (ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData : packet.chunkBiomeData()) {
			for (int i = -1; i <= 1; ++i) {
				for (int j = -1; j <= 1; ++j) {
					for (int k = thiz.level.getMinSection(); k < thiz.level.getMaxSection(); ++k) {
						thiz.minecraft.levelRenderer.setSectionDirty(transformer.xTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().x, chunkBiomeData.pos().x) + i, k, transformer.zTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().z, chunkBiomeData.pos().z) + j);
					}
				}
			}
		}
	}

	@Inject(method = "handleBlockEvent", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleBlockEvent(ClientboundBlockEventPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, (BlockableEventLoop)thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		thiz.minecraft.level.blockEvent(transformer.translateBlockFromBounds(Minecraft.getInstance().player.blockPosition(), packet.getPos()), packet.getBlock(), packet.getB0(), packet.getB1());
	}

	@Inject(method = "enableChunkLight", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void enableChunkLight(LevelChunk chunk, int x, int z, CallbackInfo ci) {
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		LevelLightEngine levelLightEngine = thiz.level.getChunkSource().getLightEngine();
		LevelChunkSection[] levelChunkSections = chunk.getSections();
		ChunkPos chunkPos = transformer.translateChunkToBounds(chunk.getPos());
		for (int i = 0; i < levelChunkSections.length; ++i) {
			LevelChunkSection levelChunkSection = levelChunkSections[i];
			int j = thiz.level.getSectionYFromSectionIndex(i);
			levelLightEngine.updateSectionStatus(SectionPos.of((ChunkPos)chunkPos, (int)j), levelChunkSection.hasOnlyAir());
			thiz.level.setSectionDirtyWithNeighbors(x, j, z);
		}
	}

	@Inject(method = "handleForgetLevelChunk", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		thiz.level.getChunkSource().drop(transformer.translateChunkFromBounds(new ChunkPos(thiz.minecraft.player.chunkPosition().x, thiz.minecraft.player.chunkPosition().z), packet.pos()));
		accessor.queueLightRemovalAM(packet);
	}

	@Inject(method = "queueLightRemoval", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void queueLightRemoval(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		ChunkPos chunkPos = transformer.translateChunkFromBounds(new ChunkPos(thiz.minecraft.player.chunkPosition().x, thiz.minecraft.player.chunkPosition().z), packet.pos());

		thiz.level.queueLightUpdate(() -> {
			int i;
			LevelLightEngine levelLightEngine = thiz.level.getLightEngine();
			levelLightEngine.setLightEnabled(chunkPos, false);
			for (i = levelLightEngine.getMinLightSection(); i < levelLightEngine.getMaxLightSection(); ++i) {
				SectionPos sectionPos = SectionPos.of((ChunkPos)chunkPos, (int)i);
				levelLightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, null);
				levelLightEngine.queueSectionData(LightLayer.SKY, sectionPos, null);
			}
			for (i = thiz.level.getMinSection(); i < thiz.level.getMaxSection(); ++i) {
				levelLightEngine.updateSectionStatus(SectionPos.of((ChunkPos)chunkPos, (int)i), true);
			}
		});
	}

	@Inject(method = "handleBlockUpdate", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, (BlockableEventLoop)thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		thiz.level.setServerVerifiedBlockState(transformer.translateBlockFromBounds(new BlockPos(thiz.minecraft.player.getBlockX(), 0, thiz.minecraft.player.getBlockZ()), packet.getPos()), packet.getBlockState(), 19);
	}

	@Inject(method = "handleMovePlayer", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, (BlockableEventLoop)thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		double i;
		double h;
		double g;
		double f;
		double e;
		double d;
		LocalPlayer player = thiz.minecraft.player;
		Vec3 vec3 = player.getDeltaMovement();
		boolean bl = packet.getRelativeArguments().contains(RelativeMovement.X);
		boolean bl2 = packet.getRelativeArguments().contains(RelativeMovement.Y);
		boolean bl3 = packet.getRelativeArguments().contains(RelativeMovement.Z);
		if (bl) {
			d = vec3.x();
			e = player.getX() + packet.getX();
			player.xOld += packet.getX();
			player.xo += packet.getX();
		} else {
			d = 0.0;
			//player.xOld = e = packet.getX();
			player.xOld = e = transformer.xTransformer.unwrapCoordFromLimit(player.getBlockX(), packet.getX());
			player.xo = e;
		}
		if (bl2) {
			f = vec3.y();
			g = player.getY() + packet.getY();
			player.yOld += packet.getY();
			player.yo += packet.getY();
		} else {
			f = 0.0;
			player.yOld = g = packet.getY();
			player.yo = g;
		}
		if (bl3) {
			h = vec3.z();
			i = player.getZ() + packet.getZ();
			player.zOld += packet.getZ();
			player.zo += packet.getZ();
		} else {
			h = 0.0;
			//player.zOld = i = packet.getZ();
			player.zOld = i = transformer.zTransformer.unwrapCoordFromLimit(player.getBlockZ(), packet.getZ());
			player.zo = i;
		}
		player.setPos(e, g, i);
		player.setDeltaMovement(d, f, h);
		float j = packet.getYRot();
		float k = packet.getXRot();
		if (packet.getRelativeArguments().contains(RelativeMovement.X_ROT)) {
			player.setXRot(player.getXRot() + k);
			player.xRotO += k;
		} else {
			player.setXRot(k);
			player.xRotO = k;
		}
		if (packet.getRelativeArguments().contains(RelativeMovement.Y_ROT)) {
			player.setYRot(player.getYRot() + j);
			player.yRotO += j;
		} else {
			player.setYRot(j);
			player.yRotO = j;
		}
		thiz.connection.send((Packet)new ServerboundAcceptTeleportationPacket(packet.getId()));
		thiz.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));
	}

	@Inject(method = "handleSetChunkCacheCenter", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread((Packet)packet, (PacketListener)thiz, (BlockableEventLoop)thiz.minecraft);
		WorldTransformer transformer = thiz.level.getTransformer();
		ci.cancel();

		accessor.getLevel().getChunkSource().updateViewCenter(transformer.xTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().x, packet.getX()), transformer.zTransformer.unwrapChunkFromLimit(thiz.minecraft.player.chunkPosition().z, packet.getZ()));
	}


}
