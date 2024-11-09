/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkTracker;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Debug(export = true)
@Mixin(ChunkTracker.class)
public abstract class ChunkTrackerMixin {

	// Is it yet needed?
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


// WrapOperation method above should be equivalent to the following commented out code.

//	@Inject(method = "checkNeighborsAfterUpdate", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
//	public void wrapppedChunkNeighbors(long pos, int level, boolean isDecreasing, CallbackInfo ci) {
//		ChunkTracker thiz = (ChunkTracker) (Object) this;
//		//TODO: Stop chunk access after bounds.
//		//ci.cancel();
//		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();
//
//		if (isDecreasing && level >= thiz.levelCount - 2) {
//			return;
//		}
//		ChunkPos chunkPos = new ChunkPos(pos);
//		int i = chunkPos.x;
//		int j = chunkPos.z;
//		for (int k = -1; k <= 1; ++k) {
//			for (int l = -1; l <= 1; ++l) {
//				long m = ChunkPos.asLong(transformer.xTransformer.wrapChunkToLimit(i + k), transformer.zTransformer.wrapChunkToLimit(j + l));
//				if (m == pos) continue;
//				thiz.checkNeighbor(pos, m, level, isDecreasing);
//			}
//		}
//	}
//


	/**
	@Redirect(method = "checkNeighborsAfterUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;asLong(II)J"))
	public long checkNeighborsAfterUpdate(int x, int z) {
		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		return ChunkPos.asLong(transformer.xTransformer.wrapChunkToLimit(x), transformer.zTransformer.wrapChunkToLimit(z));
	}**/
}
