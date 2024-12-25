/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packet;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.google.common.collect.Comparators;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "unchecked", "deprecation"})
@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {
	@Shadow
	private float batchQuota;
	@Final
	@Shadow private boolean memoryConnection;
	@Final @Shadow private LongSet pendingChunks;

	/**
	 * Collect chunks to send, prioritizing closer chunks first. Modified to include wrapped chunks as closer.
	 */
	@Inject(method = "collectChunksToSend", at = @At("HEAD"), cancellable = true)
	private void wrapChunkPosPriority(ChunkMap chunkMap, ChunkPos chunkPos, CallbackInfoReturnable<List<LevelChunk>> cir) {
		WorldTransformer transformer = chunkMap.level.getTransformer();
		cir.cancel();

		int i = Mth.floor(this.batchQuota);
		List<LevelChunk> list;
		if (!this.memoryConnection && this.pendingChunks.size() > i) {
			//list = ((List)this.pendingChunks.stream().collect(Comparators.least(i, Comparator.comparingInt(chunkPos::distanceSquared))))
			list = ((List)this.pendingChunks.stream().collect(Comparators.least(i, Comparator.comparingInt(compare -> transformer.distanceToSqrWrappedChunk(chunkPos.toLong(), compare)))))
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
				.sorted(Comparator.comparingInt(levelChunkx -> transformer.distanceToSqrWrappedChunk(levelChunkx.getPos(), chunkPos)))
				.toList();
		}

		for (LevelChunk levelChunk : list) {
			this.pendingChunks.remove(levelChunk.getPos().toLong());
		}

		cir.setReturnValue(list);
	}
}
