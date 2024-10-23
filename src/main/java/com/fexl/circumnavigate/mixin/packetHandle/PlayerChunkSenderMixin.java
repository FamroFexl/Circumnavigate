/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packetHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.google.common.collect.Comparators;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings({"unused", "unchecked", "deprecation"})
@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {
	@Shadow
	private float batchQuota;
	@Final
	@Shadow private boolean memoryConnection;
	@Final @Shadow private LongSet pendingChunks;

	@ModifyArg(method = "sendChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkWithLightPacket;<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V"), index = 0)
	private static LevelChunk sendChunk(LevelChunk chunk, @Local ServerGamePacketListenerImpl packetListener) {
		WorldTransformer transformer = packetListener.player.level().getTransformer();
		ChunkPos unwrapped = transformer.translateChunkFromBounds(packetListener.player.getClientChunk(), chunk.getPos());
		LevelChunk send = new LevelChunk(chunk.level, unwrapped, chunk.getUpgradeData(), (LevelChunkTicks<Block>) chunk.getBlockTicks(), (LevelChunkTicks<Fluid>) chunk.getFluidTicks(), chunk.getInhabitedTime(), chunk.getSections(), chunk.postLoad, chunk.getBlendingData());
		return send;
	}

	/**
	 * Collect chunks to send, prioritizing closer chunks first. Modified to include wrapped chunks as closer.
	 */
	@Inject(method = "collectChunksToSend", at = @At("HEAD"), cancellable = true)
	private void collectChunksToSend(ChunkMap chunkMap, ChunkPos chunkPos, CallbackInfoReturnable<List<LevelChunk>> cir) {
		WorldTransformer transformer = chunkMap.level.getTransformer();
		cir.cancel();

		int i = Mth.floor(this.batchQuota);
		List<LevelChunk> list;
		if (!this.memoryConnection && this.pendingChunks.size() > i) {
			//list = ((List)this.pendingChunks.stream().collect(Comparators.least(i, Comparator.comparingInt(chunkPos::distanceSquared))))
			list = ((List)this.pendingChunks.stream().collect(Comparators.least(i, Comparator.comparingInt(compare -> transformer.distanceToSqrWrapped(chunkPos.toLong(), compare)))))
				.stream()
				.mapToLong(longValue -> (long) longValue)
				.mapToObj(chunkMap::getChunkToSend)
				.filter(Objects::nonNull)
				.toList();
		} else {
			list = this.pendingChunks
				.longStream()
				.mapToObj(chunkMap::getChunkToSend)
				.filter(Objects::nonNull)
				//.sorted(Comparator.comparingInt(levelChunkx -> chunkPos.distanceSquared(levelChunkx.getPos())))
				.sorted(Comparator.comparingInt(levelChunkx -> transformer.distanceToSqrWrapped(levelChunkx.getPos(), chunkPos)))
				.toList();
		}

		for (LevelChunk levelChunk : list) {
			this.pendingChunks.remove(levelChunk.getPos().toLong());
		}

		cir.setReturnValue(list);
	}
}
