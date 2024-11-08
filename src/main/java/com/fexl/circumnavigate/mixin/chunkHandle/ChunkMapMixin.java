/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
	@Final @Shadow public ServerLevel level;
	@Final @Shadow private ThreadedLevelLightEngine lightEngine;
	@Final @Shadow private ChunkTaskPriorityQueueSorter queueSorter;
	@Shadow abstract ChunkHolder updateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);

	ChunkMap thiz = (ChunkMap) (Object) this;

	/**
	 * Stores the serverLevel for usage further down the call chain where it was not passed.
	 * Modifies the parameters to use client-wrapped chunks.
	 */
	@Inject(method = "isChunkTracked", at = @At("HEAD"), cancellable = true)
	public void unwrapChunkPosForCheck(ServerPlayer player, int x, int z, CallbackInfoReturnable<Boolean> cir) {
		TransformerRequests.chunkMapLevel = player.serverLevel();
		WorldTransformer transformer = player.serverLevel().getTransformer();
		cir.setReturnValue(player.getChunkTrackingView().contains(x, z) && !player.connection.chunkSender.isPending(ChunkPos.asLong(transformer.xTransformer.unwrapChunkFromLimit(player.getClientChunk().x, x), transformer.zTransformer.unwrapChunkFromLimit(player.getClientChunk().z, z))));
	}

	/**
	 * Stores the serverLevel for usage further down the call chain where it was not passed.
	 */
	@Inject(method = "applyChunkTrackingView", at = @At("HEAD"))
	public void captureLevel(ServerPlayer player, ChunkTrackingView chunkTrackingView, CallbackInfo ci) {
		TransformerRequests.chunkMapLevel = player.serverLevel();
	}

	@Inject(method = "euclideanDistanceSquared", at = @At("HEAD"), cancellable = true)
    private static void wrapDistanceToSquare(ChunkPos chunkPos, Entity entity, CallbackInfoReturnable<Double> cir) {
		double d = (double)SectionPos.sectionToBlockCoord(chunkPos.x, 8);
		double e = (double)SectionPos.sectionToBlockCoord(chunkPos.z, 8);
		cir.setReturnValue(entity.level().getTransformer().distanceToSqrWrapped(entity.getX(), 0, entity.getY(), d, 0, e));
	}

	/**
	@Inject(method = "updateChunkScheduling", at = @At("HEAD"), cancellable = true)
	public void updateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel, CallbackInfoReturnable<ChunkHolder> cir) {
		if(!level.getTransformer().isChunkOverBounds(new ChunkPos(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos)))) return;
		long wrappedChunkPos = level.getTransformer().translateChunkToBounds(chunkPos);
		ChunkHolder wrappedChunkHolder;

		if(holder == null) wrappedChunkHolder = null;
		else wrappedChunkHolder = new ChunkHolder(level.getTransformer().translateChunkToBounds(holder.getPos()), holder.getTicketLevel(), level, lightEngine, queueSorter, thiz);

		this.updateChunkScheduling(wrappedChunkPos, newLevel, wrappedChunkHolder, oldLevel);
	}**/


}
