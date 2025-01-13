/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunk;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@Shadow @Final ServerLevel level;

	//TODO: Very slow, but optimal injection
	/**
	@ModifyVariable(method = "getChunkNow", at = @At("HEAD"), argsOnly = true, index = 1)
	public int modifyX(int chunkX) {
		return level.getTransformer().Chunk.X.wrapToBounds(chunkX);
	}

	@ModifyVariable(method = "getChunkNow", at = @At("HEAD"), argsOnly = true, index = 2)
	public int modifyZ(int chunkZ) {
		return level.getTransformer().Chunk.Z.wrapToBounds(chunkZ);
	}**/
}
