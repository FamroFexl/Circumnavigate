/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.SectionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionTracker.class)
public class SectionTrackerMixin {

	/**
	 * Updates loading levels of adjacent chunk sections so they are ready when needed. Modified to include wrapped sections.
	 */
	@WrapOperation(method = "checkNeighborsAfterUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;offset(JIII)J"))
	private long wrapChunkPos(long pos, int x, int y, int z, Operation<Long> original, @Local(argsOnly = true) int level, @Local(argsOnly = true) boolean isDecreasing) {
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		int wrappedX = transformer.xTransformer.wrapChunkToLimit(x);
		int wrappedZ = transformer.zTransformer.wrapChunkToLimit(z);
		long chunkLong = original.call(pos, wrappedX, y, wrappedZ);

		if (chunkLong != pos) {
			SectionTracker thiz = (SectionTracker) (Object) this;
			thiz.checkNeighbor(pos, chunkLong, level, isDecreasing);
		}

		return original.call(pos, x, y, z);
	}
}
