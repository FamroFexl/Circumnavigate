/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ChunkTracker.class)
public abstract class ChunkTrackerMixin extends DynamicGraphMinFixedPoint {

	protected ChunkTrackerMixin(int firstQueuedLevel, int width, int height) {
		super(firstQueuedLevel, width, height);
	}

	@Shadow protected abstract int computeLevelFromNeighbor(long startPos, long endPos, int startLevel);

	/**
	 * Modifies ChunkPos to use wrapped chunks. This is essentially what fixes #10.
	 */
	@WrapOperation(method = "getComputedLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTracker;computeLevelFromNeighbor(JJI)I"))
	private int getComputedLevel(ChunkTracker instance, long startPos, long endPos, int startLevel, Operation<Integer> original, @Local(name = "excludedSourcePos") long excludedSourcePos) {
		int originalRet = original.call(instance, startPos, endPos, startLevel);

		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		ChunkPos chunkPos = new ChunkPos(startPos);
		ChunkPos wrappedChunkPos = transformer.translateChunkToBounds(chunkPos);
		long wrappedPos = wrappedChunkPos.toLong();

		if (wrappedPos == endPos) {
			wrappedPos = ChunkPos.INVALID_CHUNK_POS;
		}

		if (wrappedPos != excludedSourcePos) {
			int wrappedRet = this.computeLevelFromNeighbor(wrappedPos, endPos, getLevel(wrappedPos));

			// return the lower of the two levels
			return Math.min(originalRet, wrappedRet);
		}

		return originalRet;
    }

	/**
	 * Updates loading levels of adjacent chunks so they are ready when needed. Modified to include wrapped chunks.
	 */
	@WrapOperation(method = "checkNeighborsAfterUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;asLong(II)J"))
	private long wrapChunkPos(int x, int z, Operation<Long> original, @Local(argsOnly = true) long pos, @Local(argsOnly = true) int level, @Local(argsOnly = true) boolean isDecreasing) {
		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		int wrappedX = transformer.xTransformer.wrapChunkToLimit(x);
		int wrappedZ = transformer.zTransformer.wrapChunkToLimit(z);
		long chunkLong = original.call(wrappedX, wrappedZ);

		if (chunkLong != pos) {
			ChunkTracker thiz = (ChunkTracker) (Object) this;
			thiz.checkNeighbor(pos, chunkLong, level, isDecreasing);
		}

		return original.call(x, z);
	}
}
