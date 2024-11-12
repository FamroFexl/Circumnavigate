/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ChunkTrackingView.class)
public interface ChunkTrackingViewMixin extends ChunkTrackingView {
	/**
	 * Checks if a chunk is within a distance. Modified to support wrapping.
	 */
	@Inject(method = "isWithinDistance", at = @At("HEAD"), cancellable = true)
	private static void checkWrappedChunks(int centerX, int centerZ, int viewDistance, int x, int z, boolean serachAllChunks, CallbackInfoReturnable<Boolean> cir) {
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		//Don't include chunks that extend past the bounds.
		if(transformer.xTransformer.isChunkOverLimit(x) || transformer.zTransformer.isChunkOverLimit(z)) { cir.setReturnValue(false); return; }

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

	/**
	 * Functional method which outputs chunks to remove and chunks to add. Modified to support wrapping.
	 */
	@Inject(method = "difference", at = @At("HEAD"), cancellable = true)
	private static void includeWrappedChunks(ChunkTrackingView oldChunkTrackingView, ChunkTrackingView newChunkTrackingView, Consumer<ChunkPos> chunkDropper, Consumer<ChunkPos> chunkMarker, CallbackInfo ci) {
		ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		if (oldChunkTrackingView.equals(newChunkTrackingView)) return;

		if (oldChunkTrackingView instanceof ChunkTrackingView.Positioned positioned
			&& newChunkTrackingView instanceof ChunkTrackingView.Positioned positioned2
			&& ((PositionedAccessorMixin) (Object) positioned).squareIntersectsAM(positioned2))
		{

			//This prevents mass calculation of unneeded chunks and keeps chunk bandwidth predictable when crossing borders
			int i = Math.min(positioned.minX(), transformer.xTransformer.unwrapChunkFromLimit(positioned.minX(), positioned2.minX()));
			int j = Math.min(positioned.minZ(), transformer.zTransformer.unwrapChunkFromLimit(positioned.minZ(), positioned2.minZ()));
			int k = Math.max(positioned.maxX(), transformer.xTransformer.unwrapChunkFromLimit(positioned.maxX(), positioned2.maxX()));
			int l = Math.max(positioned.maxZ(), transformer.zTransformer.unwrapChunkFromLimit(positioned.maxZ(), positioned2.maxZ()));

			for (int x = i; x <= k; x++) {
				for (int z = j; z <= l; z++) {

					int wrappedX = transformer.xTransformer.wrapChunkToLimit(x);
					int wrappedZ = transformer.zTransformer.wrapChunkToLimit(z);

					boolean bl = positioned.contains(wrappedX, wrappedZ);
					boolean bl2 = positioned2.contains(wrappedX, wrappedZ);
					if (bl != bl2) {
						if (bl2) {
							//Chunk exists in new
							chunkDropper.accept(new ChunkPos(wrappedX, wrappedZ));
						} else {
							//Chunk exists in old
							chunkMarker.accept(new ChunkPos(wrappedX, wrappedZ));
						}
					}
				}
			}

			return;
		}

		oldChunkTrackingView.forEach(chunkMarker);
		newChunkTrackingView.forEach(chunkDropper);
	}
}
