/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.propagators.chunkTracker.providers;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.mixin.propagators.chunkTracker.senders.DistanceManagerMixin;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

/**
 * Pass to {@link DistanceManagerMixin}
 */
@Mixin(ChunkMap.DistanceManager.class)
public class ChunkMap$DistanceManagerMixin extends DistanceManagerMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(ChunkMap chunkMap, Executor dispatcher, Executor mainThreadExecutor, CallbackInfo ci) {
		//Assigns a WorldTransformer to the DistanceManager (this is the only place it is used)
		((TransformerAccessor) this).setTransformer(chunkMap.level.getTransformer());

		//Initializes WorldTransformers for ChunkTracker instances since they couldn't be assigned in the superclasses' constructor.
		this.assignTransformers();
	}
}
