/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.server.level.ChunkTrackingView.Positioned;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(Positioned.class)
public abstract class PositionedMixin {

	@Unique private Positioned thiz = (Positioned) (Object) this;
	/**
	 * Modified to ensure when the player is on a chunk boundary, and login, they will get the correct chunks from both sides of the world. This method is only used on login.
	 */
	@Inject(method = "forEach", at = @At("HEAD"), cancellable = true)
	private void includeWrappedChunks(Consumer<ChunkPos> action, CallbackInfo ci) {
		ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		for (int x = thiz.minX(); x <= thiz.maxX(); x++) {
			for (int z = thiz.minZ(); z <= thiz.maxZ(); z++) {

				int wrappedX = transformer.xTransformer.wrapChunkToLimit(x);
				int wrappedZ = transformer.zTransformer.wrapChunkToLimit(z);

				if (((Positioned)(Object)this).contains(wrappedX, wrappedZ)) {
					action.accept(new ChunkPos(wrappedX, wrappedZ));
				}
			}
		}
	}

	/**
	 * Modified to ensure chunks aren't unnecessarily sent or dropped when the player is teleported across the world bounds.
	 */
	@Inject(method = "squareIntersects(Lnet/minecraft/server/level/ChunkTrackingView$Positioned;)Z", at = @At("HEAD"), cancellable = true)
	protected void includeWrappedChunks(Positioned other, CallbackInfoReturnable<Boolean> cir) {
		cir.cancel();

		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();
		boolean xIntersects = (thiz.minX() <= other.maxX() && thiz.maxX() >= other.minX()) ||
			(thiz.minX() + transformer.xWidth <= other.maxX() && thiz.maxX() + transformer.xWidth >= other.minX()) ||
			(thiz.minX() <= other.maxX() + transformer.xWidth && thiz.maxX() >= other.minX() + transformer.xWidth);

		boolean zIntersects = (thiz.minZ() <= other.maxZ() && thiz.maxZ() >= other.minZ()) ||
			(thiz.minZ() + transformer.zWidth <= other.maxZ() && thiz.maxZ() + transformer.zWidth >= other.minZ()) ||
			(thiz.minZ() <= other.maxZ() + transformer.zWidth && thiz.maxZ() >= other.minZ() + transformer.zWidth);

		cir.setReturnValue(xIntersects && zIntersects);
	}
}
