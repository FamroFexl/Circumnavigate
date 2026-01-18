/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunk;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.core.DimensionTransformer;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.server.level.ChunkTrackingView.Positioned;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@Mixin(Positioned.class)
public abstract class ChunkTrackingView$PositionedMixin implements TransformerAccessor {

	@Unique private Positioned thiz = (Positioned) (Object) this;
	/**
	 * Modified to ensure when the player is on a chunk boundary, and login, they will get the correct chunks from both sides of the world. This method is only used on login.
	 *
	 * @author Famro Fexl
	 * @reason Wrapped Worlds require including wrapped chunks in calculations.
	 */
	@Overwrite
	public void forEach(Consumer<ChunkPos> action) {
		for (int x = thiz.minX(); x <= thiz.maxX(); x++) {
			for (int z = thiz.minZ(); z <= thiz.maxZ(); z++) {

				int wrappedX = transformer.Chunk.X.wrap(x);
				int wrappedZ = transformer.Chunk.Z.wrap(z);

				if (((Positioned)(Object)this).contains(wrappedX, wrappedZ)) {
					action.accept(new ChunkPos(wrappedX, wrappedZ));
				}
			}
		}
	}

	/**
	 * Modified to ensure chunks aren't unnecessarily sent or dropped when the player is teleported across the world bounds.
	 *
	 * @author Famro Fexl
	 * @reason Intersections must be wrapped in a Wrapped World.
	 */
	@VisibleForTesting
	@Overwrite
	public boolean squareIntersects(Positioned other) {
		boolean xIntersects = (thiz.minX() <= other.maxX() && thiz.maxX() >= other.minX()) ||
			(thiz.minX() + transformer.xWidth <= other.maxX() && thiz.maxX() + transformer.xWidth >= other.minX()) ||
			(thiz.minX() <= other.maxX() + transformer.xWidth && thiz.maxX() >= other.minX() + transformer.xWidth);

		boolean zIntersects = (thiz.minZ() <= other.maxZ() && thiz.maxZ() >= other.minZ()) ||
			(thiz.minZ() + transformer.zWidth <= other.maxZ() && thiz.maxZ() + transformer.zWidth >= other.minZ()) ||
			(thiz.minZ() <= other.maxZ() + transformer.zWidth && thiz.maxZ() >= other.minZ() + transformer.zWidth);

		return xIntersects && zIntersects;
	}

	DimensionTransformer transformer;

	@Override
	public DimensionTransformer getTransformer() {
		return this.transformer;
	}

	@Override
	public void setTransformer(DimensionTransformer transformer) {
		this.transformer = transformer;
	}
}
