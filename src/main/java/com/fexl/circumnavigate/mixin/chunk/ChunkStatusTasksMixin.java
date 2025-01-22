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

/**
 * Cancels chunk generation beyond wrapping bounds for all generation steps.
 */
@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {
	@Inject(method = "generateStructureStarts", at = @At("HEAD"), cancellable = true)
	private static void generateStructureStarts(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateStructureReferences", at = @At("HEAD"), cancellable = true)
	private static void generateStructureReferences(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateBiomes", at = @At("HEAD"), cancellable = true)
	private static void generateBiomes(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateNoise", at = @At("HEAD"), cancellable = true)
	private static void generateNoise(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateSurface", at = @At("HEAD"), cancellable = true)
	private static void generateSurface(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateCarvers", at = @At("HEAD"), cancellable = true)
	private static void generateCarvers(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateFeatures", at = @At("HEAD"), cancellable = true)
	private static void generateFeatures(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	@Inject(method = "generateSpawn", at = @At("HEAD"), cancellable = true)
	private static void generateSpawn(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
		else {
			TransformerRequests.noiseLevel = worldGenContext.level();
		}
	}

	private boolean overBounds(WorldGenContext worldGenContext, ChunkAccess chunk) {
		if(worldGenContext.level().getTransformer().Chunk.isOverBounds(chunk.getPos())) {
			return true;
		}
		TransformerRequests.noiseLevel = worldGenContext.level();
		return false;
	}
}
