/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.propagators.chunkTracker.senders;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.TickingTracker;
import org.spongepowered.asm.mixin.*;

/**
 * Pass to {@link com.fexl.circumnavigate.mixin.chunk.ChunkTrackerMixin} instances
 */
@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements TransformerAccessor {
	@Mutable @Shadow @Final private DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter;
	@Mutable @Shadow @Final private DistanceManager.PlayerTicketTracker playerTicketManager;
	@Mutable @Shadow @Final private DistanceManager.ChunkTicketTracker ticketTracker;
	@Mutable @Shadow @Final private TickingTracker tickingTicketsTracker;

	/**
	 * Sets the transformers for the trackers. This cannot be called before DistanceManager's constructor because the child class ChunkMap.DistanceManager needs to call its super method (DistanceManager's constructor) before it can assign a transformer to the class.
	 */
	@Unique
	public void assignTransformers() {
		this.naturalSpawnChunkCounter.setTransformer(this.getTransformer());
		this.playerTicketManager.setTransformer(this.getTransformer());
		this.ticketTracker.setTransformer(this.getTransformer());
		this.tickingTicketsTracker.setTransformer(this.getTransformer());
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
