/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkTracker.class)
public class ChunkTrackerMixin {

	/**
	 * Updates loading levels of adjacent chunks so they are ready when needed. Modified to include wrapped chunks.
	 */
	@Inject(method = "checkNeighborsAfterUpdate", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void wrapppedChunkNeighbors(long pos, int level, boolean isDecreasing, CallbackInfo ci) {
		ChunkTracker thiz = (ChunkTracker) (Object) this;
		//TODO: Stop chunk access after bounds.
		//ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		if (isDecreasing && level >= thiz.levelCount - 2) {
			return;
		}
		ChunkPos chunkPos = new ChunkPos(pos);
		int i = chunkPos.x;
		int j = chunkPos.z;
		for (int k = -1; k <= 1; ++k) {
			for (int l = -1; l <= 1; ++l) {
				long m = ChunkPos.asLong(transformer.xTransformer.wrapChunkToLimit(i + k), transformer.zTransformer.wrapChunkToLimit(j + l));
				if (m == pos) continue;
				thiz.checkNeighbor(pos, m, level, isDecreasing);
			}
		}
	}

	/**
	@Redirect(method = "checkNeighborsAfterUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;asLong(II)J"))
	public long checkNeighborsAfterUpdate(int x, int z) {
		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		return ChunkPos.asLong(transformer.xTransformer.wrapChunkToLimit(x), transformer.zTransformer.wrapChunkToLimit(z));
	}**/
}
