/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.function.Consumer;

@Mixin(ChunkTrackingView.class)
public interface ChunkTrackingViewMixin extends ChunkTrackingView {
	@Inject(method = "isWithinDistance", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private static void isWithinDistance(int centerX, int centerZ, int viewDistance, int x, int z, boolean serachAllChunks, CallbackInfoReturnable<Boolean> cir) {
		cir.cancel();
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		//Don't include chunks that extend past the bounds.
		if(x < transformer.xChunkBoundMin || x > transformer.xChunkBoundMax - 1 || z < transformer.zChunkBoundMin || z > transformer.zChunkBoundMax - 1) {
			return;
		}

		int unwrappedX = transformer.xTransformer.unwrapChunkFromLimit(centerX, x);
		int unwrappedZ = transformer.zTransformer.unwrapChunkFromLimit(centerZ, z);

		int i = Math.max(0, Math.abs(unwrappedX - centerX) - 1);
		int j = Math.max(0, Math.abs(unwrappedZ - centerZ) - 1);

		long l = Math.max(0, Math.max(i, j) - (serachAllChunks ? 1 : 0));
		long m = Math.min(i, j);
		long n = m * m + l * l;
		int k = viewDistance * viewDistance;
		cir.setReturnValue(n < (long)k);

	}

	@Inject(method = "difference", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private static void difference(ChunkTrackingView oldChunkTrackingView, ChunkTrackingView newChunkTrackingView, Consumer<ChunkPos> chunkDropper, Consumer<ChunkPos> chunkMarker, CallbackInfo ci) {
		ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		Positioned positioned2;
		Positioned positioned;
		block8: {
			block7: {
				if (oldChunkTrackingView.equals(newChunkTrackingView)) {
					return;
				}
				if (!(oldChunkTrackingView instanceof Positioned)) break block7;
				positioned = (Positioned)oldChunkTrackingView;
				if (newChunkTrackingView instanceof Positioned && ((PositionedAccessorMixin)(Object)positioned).squareIntersectsAM(positioned2 = (Positioned)newChunkTrackingView)) break block8;
			}

			oldChunkTrackingView.forEach(chunkMarker);
			newChunkTrackingView.forEach(chunkDropper);
			return;
		}
		int i = Math.min(positioned.minX(), positioned2.minX());
		int j = Math.min(positioned.minZ(), positioned2.minZ());
		int k = Math.max(positioned.maxX(), positioned2.maxX());
		int l = Math.max(positioned.maxZ(), positioned2.maxZ());
		int m = i;

		while (m <= k) {
			for (int n = j; n <= l; ++n) {
				boolean bl2;
				boolean bl = positioned.contains(transformer.xTransformer.wrapChunkToLimit(m), transformer.zTransformer.wrapChunkToLimit(n));
				if (bl == (bl2 = positioned2.contains(transformer.xTransformer.wrapChunkToLimit(m), transformer.zTransformer.wrapChunkToLimit(n)))) continue;
				if (bl2) {
					chunkDropper.accept(new ChunkPos(transformer.xTransformer.wrapChunkToLimit(m), transformer.zTransformer.wrapChunkToLimit(n)));
					continue;
				}
				chunkMarker.accept(new ChunkPos(transformer.xTransformer.wrapChunkToLimit(m), transformer.zTransformer.wrapChunkToLimit(n)));
			}
			++m;
		}
		return;
	}

}
