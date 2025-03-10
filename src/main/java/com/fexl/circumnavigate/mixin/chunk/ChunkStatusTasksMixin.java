/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunk;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;

import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {
	/**
	 * Cancels chunk generation beyond wrapping bounds for all generation steps.
	 */
	@Inject(method = {"generateStructureStarts", "generateStructureReferences", "generateBiomes", "generateNoise", "generateSurface", "generateCarvers", "generateFeatures", "generateSpawn"}, at = @At("HEAD"), cancellable = true)
	private static void targetAll(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		//Set the noise level for world transformers
		TransformerRequests.noiseLevel = worldGenContext.level();

		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) cir.setReturnValue(CompletableFuture.completedFuture(chunk));
	}
}
