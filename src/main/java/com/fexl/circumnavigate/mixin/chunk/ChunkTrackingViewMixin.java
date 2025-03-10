/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunk;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ChunkTrackingView.class)
public interface ChunkTrackingViewMixin {
	/**
	 * Checks if a chunk is within a distance. Modified to support wrapping.
	 */
	@Inject(method = "isWithinDistance", at = @At("HEAD"), cancellable = true)
	private static void checkWrappedChunks(int centerX, int centerZ, int viewDistance, int x, int z, boolean serachAllChunks, CallbackInfoReturnable<Boolean> cir) {
		//Because isWithinDistance is a static method, it requires an exterior transformer instance that can't be passed down.
		DimensionTransformer transformer = TransformerRequests.chunkMapTransformer.onlyServerSide();

		//Don't include chunks that extend past the bounds.
		if(transformer.Chunk.X.isOverBounds(x) || transformer.Chunk.Z.isOverBounds(z)) cir.setReturnValue(false);
	}

	/**
	 * Unwrap X Chunk position
	 */
	@ModifyVariable(method = "isWithinDistance", at = @At("HEAD"), index = 3, argsOnly = true)
	private static int modifyX(int x, @Local(index = 0, argsOnly = true) int centerX) {
		DimensionTransformer transformer = TransformerRequests.chunkMapTransformer.onlyServerSide();

		return transformer.Chunk.X.unwrapFromBounds(centerX, x);
	}

	/**
	 * Unwrap Z Chunk position
	 */
	@ModifyVariable(method = "isWithinDistance", at = @At("HEAD"), index = 4, argsOnly = true)
	private static int modifyZ(int z, @Local(index = 1, argsOnly = true) int centerZ) {
		DimensionTransformer transformer = TransformerRequests.chunkMapTransformer.onlyServerSide();

		return transformer.Chunk.Z.unwrapFromBounds(centerZ, z);
	}

	/**
	 * @author Famro Fexl
	 * @reason World wrapping includes wrapped chunks, and each chunk must be wrapped in order to be properly checked.
	 */
	@Overwrite
	static void difference(ChunkTrackingView oldChunkTrackingView, ChunkTrackingView newChunkTrackingView, Consumer<ChunkPos> chunkMarker, Consumer<ChunkPos> chunkDropper) {
		if (oldChunkTrackingView.equals(newChunkTrackingView)) return;

		if (oldChunkTrackingView instanceof ChunkTrackingView.Positioned positioned
			&& newChunkTrackingView instanceof ChunkTrackingView.Positioned positioned2)
			if (((PositionedAccessorMixin) (Object) positioned).squareIntersectsAM(positioned2)) {

				DimensionTransformer transformer = ((TransformerAccessor) (Object) positioned).getTransformer();

				//This prevents mass calculation of unneeded chunks and keeps chunk bandwidth predictable when crossing borders
				int minX = Math.min(positioned.minX(), transformer.Chunk.X.unwrapFromBounds(positioned.minX(), positioned2.minX()));
				int minZ = Math.min(positioned.minZ(), transformer.Chunk.Z.unwrapFromBounds(positioned.minZ(), positioned2.minZ()));
				int maxX = Math.max(positioned.maxX(), transformer.Chunk.X.unwrapFromBounds(positioned.maxX(), positioned2.maxX()));
				int maxZ = Math.max(positioned.maxZ(), transformer.Chunk.Z.unwrapFromBounds(positioned.maxZ(), positioned2.maxZ()));

				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {

						int wrappedX = transformer.Chunk.X.wrapToBounds(x);
						int wrappedZ = transformer.Chunk.Z.wrapToBounds(z);

						boolean inOld = positioned.contains(wrappedX, wrappedZ);
						boolean inNew = positioned2.contains(wrappedX, wrappedZ);
						if (inOld != inNew) {
							if (inNew) {
								//Chunk exists in new
								chunkMarker.accept(new ChunkPos(wrappedX, wrappedZ));
							} else {
								//Chunk exists in old
								chunkDropper.accept(new ChunkPos(wrappedX, wrappedZ));
							}
						}
					}
				}

				return;
			}

		oldChunkTrackingView.forEach(chunkDropper);
		newChunkTrackingView.forEach(chunkMarker);
	}
}
