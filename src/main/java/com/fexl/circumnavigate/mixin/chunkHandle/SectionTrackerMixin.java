/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SectionTracker.class)
public class SectionTrackerMixin {
	SectionTracker thiz = (SectionTracker) (Object) this;

	/**
	 * Updates loading levels of adjacent chunk sections so they are ready when needed. Modified to include wrapped sections.
	 */
	@Inject(method = "checkNeighborsAfterUpdate", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void wrappedSectionNeighbors(long pos, int level, boolean isDecreasing, CallbackInfo ci) {
		//TODO: Stop sub-chunk access after bounds.
		//ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		if (isDecreasing && level >= thiz.levelCount - 2) {
			return;
		}
		for (int x = -1; x <= 1; ++x) {
			for (int y = -1; y <= 1; ++y) {
				for (int z = -1; z <= 1; ++z) {
					long l = SectionPos.offset(pos, transformer.xTransformer.wrapChunkToLimit(x), y, transformer.zTransformer.wrapChunkToLimit(y));
					if (l == pos) continue;
					thiz.checkNeighbor(pos, l, level, isDecreasing);
				}
			}
		}
	}
}
