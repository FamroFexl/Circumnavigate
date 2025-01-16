/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathNavigationRegion.class)
public class PathNavigationRegionMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(Level level, BlockPos centerPos, BlockPos offsetPos, CallbackInfo ci) {
		PathNavigationRegion thiz = (PathNavigationRegion) (Object) this;

		thiz.setTransformer(level.getTransformer().onlyServerSide());
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkSource;getChunkNow(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
	public LevelChunk init(ChunkSource instance, int chunkX, int chunkZ, @Local(argsOnly = true) Level level) {
		DimensionTransformer transformer = level.getTransformer().onlyServerSide();
		return instance.getChunkNow(transformer.Chunk.X.wrapToBounds(chunkX), transformer.Chunk.Z.wrapToBounds(chunkZ));
	}
}
