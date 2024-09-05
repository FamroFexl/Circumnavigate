/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
	/**
	 * Stores the serverLevel for usage further down the call chain where it was not passed.
	 */
	@Inject(method = "isChunkTracked", at = @At("HEAD"))
	public void isChunkTracked(ServerPlayer player, int x, int z, CallbackInfoReturnable<Boolean> cir) {
		TransformerRequests.chunkMapLevel = player.serverLevel();
	}

	/**
	 * Stores the serverLevel for usage further down the call chain where it was not passed.
	 */
	@Inject(method = "applyChunkTrackingView", at = @At("HEAD"))
	public void applyChunkTrackingView(ServerPlayer player, ChunkTrackingView chunkTrackingView, CallbackInfo ci) {
		TransformerRequests.chunkMapLevel = player.serverLevel();
	}
}
