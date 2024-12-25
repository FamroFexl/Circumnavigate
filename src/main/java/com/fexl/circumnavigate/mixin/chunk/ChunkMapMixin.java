/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunk;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

	@Final @Shadow public ServerLevel level;

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
	 * Gives ChunkTrackingView.Positioned instances a WorldTransformer when they are created (this is the only place they are created)
	 */
	@Redirect(method = "updateChunkTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTrackingView;of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/server/level/ChunkTrackingView;"))
	public ChunkTrackingView updateChunkTracking(ChunkPos center, int viewDistance) {
		ChunkTrackingView.Positioned newView = (ChunkTrackingView.Positioned) ChunkTrackingView.of(center, viewDistance);
		((TransformerAccessor) (Object) newView).setTransformer(level.getTransformer());
		return newView;
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
		double d = SectionPos.sectionToBlockCoord(chunkPos.x, 8);
		double e = SectionPos.sectionToBlockCoord(chunkPos.z, 8);
		cir.setReturnValue(entity.level().getTransformer().distanceToSqrWrappedCoord(entity.getX(), 0, entity.getY(), d, 0, e));
	}
}
