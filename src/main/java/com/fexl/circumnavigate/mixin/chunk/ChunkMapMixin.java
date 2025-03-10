/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunk;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.core.DimensionTransformer;
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
	 * Players will track wrapped chunks as part of their tracking view.
	 */
	@Inject(method = "isChunkTracked", at = @At("HEAD"), cancellable = true)
	public void isChunkTracked(ServerPlayer player, int x, int z, CallbackInfoReturnable<Boolean> cir) {
		DimensionTransformer transformer = player.serverLevel().getTransformer();

		//Stores the serverLevel for usage further down the call chain where it was not passed.
		TransformerRequests.chunkMapTransformer = transformer;

		cir.setReturnValue(player.getChunkTrackingView().contains(x, z) && !player.connection.chunkSender.isPending(ChunkPos.asLong(transformer.Chunk.X.unwrapFromBounds(player.getClientChunk().x, x), transformer.Chunk.Z.unwrapFromBounds(player.getClientChunk().z, z))));
	}

	/**
	 * Gives ChunkTrackingView.Positioned instances a WorldTransformer when they are created (this is the only place they are created)
	 */
	@Redirect(method = "updateChunkTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTrackingView;of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/server/level/ChunkTrackingView;"))
	public ChunkTrackingView setChunkTransformer(ChunkPos center, int viewDistance) {
		ChunkTrackingView.Positioned newView = (ChunkTrackingView.Positioned) ChunkTrackingView.of(center, viewDistance);
		((TransformerAccessor) (Object) newView).setTransformer(level.getTransformer());
		return newView;
	}

	/**
	 * Stores the serverLevel for usage further down the call chain where it was not passed.
	 */
	@Inject(method = "applyChunkTrackingView", at = @At("HEAD"))
	public void captureLevel(ServerPlayer player, ChunkTrackingView chunkTrackingView, CallbackInfo ci) {
		TransformerRequests.chunkMapTransformer = player.serverLevel().getTransformer();
	}

	/**
	 * Support wrapped distances as closest Euclidean distance.
	 */
	@Inject(method = "euclideanDistanceSquared", at = @At("HEAD"), cancellable = true)
    private static void euclideanDistanceSquared(ChunkPos chunkPos, Entity entity, CallbackInfoReturnable<Double> cir) {
		double d = SectionPos.sectionToBlockCoord(chunkPos.x, 8);
		double e = SectionPos.sectionToBlockCoord(chunkPos.z, 8);
		cir.setReturnValue(entity.level().getTransformer().Coord.sqrDistToBounds(entity.getX(), 0, entity.getY(), d, 0, e));
	}
}
