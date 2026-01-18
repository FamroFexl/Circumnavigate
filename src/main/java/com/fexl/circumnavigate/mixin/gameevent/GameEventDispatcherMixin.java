/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.gameevent;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameEventDispatcher.class)
public abstract class GameEventDispatcherMixin {
	@Shadow @Final private ServerLevel level;

	/**
	 * Wrap distance calculation for game events dependent on minimum distances.
	 */
	@ModifyArg(method = "method_45492", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gameevent/GameEvent$ListenerInfo;<init>(Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Context;Lnet/minecraft/world/level/gameevent/GameEventListener;Lnet/minecraft/world/phys/Vec3;)V"))
	public Vec3 modifyDist(Vec3 listener, @Local(argsOnly = true, ordinal = 0) Vec3 event) {
		return level.getTransformer().Vector3D.unwrap(event, listener);
	}

	@Redirect(method = "post", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getChunkNow(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
	public LevelChunk postChunkMix(ServerChunkCache instance, int chunkX, int chunkZ) {
		return this.level.getChunkSource().getChunkNow(level.getTransformer().Chunk.X.wrap(chunkX), level.getTransformer().Chunk.Z.wrap(chunkZ));
	}
}
