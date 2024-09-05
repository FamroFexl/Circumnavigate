/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.util.WorldTransformer;
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

	//ModifyArgs and ModifyArg do not work for this. They remove a return statement in the original method.
	@Inject(method = "checkNeighborsAfterUpdate", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void checkNeighbors(long pos, int level, boolean isDecreasing, CallbackInfo ci) {
		//TODO: Stop sub-chunk access after bounds.
		//ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkCacheLevel.getTransformer();

		if (isDecreasing && level >= thiz.levelCount - 2) {
			return;
		}
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				for (int k = -1; k <= 1; ++k) {
					long l = SectionPos.offset(pos, transformer.xTransformer.wrapChunkToLimit(i), j, transformer.zTransformer.wrapChunkToLimit(j));
					//long l = SectionPos.offset(pos, i, j, k);
					if (l == pos) continue;
					thiz.checkNeighbor(pos, l, level, isDecreasing);
				}
			}
		}
	}
}
