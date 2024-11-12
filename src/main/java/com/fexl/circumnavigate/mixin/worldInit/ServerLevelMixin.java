/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	/**
	 * Set the wrapping settings for each level when it is created as quickly as possible.
	 */
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/LevelStem;generator()Lnet/minecraft/world/level/chunk/ChunkGenerator;"))
	public void init(MinecraftServer server, Executor dispatcher, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey dimension, LevelStem levelStem, ChunkProgressListener progressListener, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime, RandomSequences randomSequences, CallbackInfo ci) {
		ServerLevel thiz = (ServerLevel) (Object) this;

		if(dimension.equals(Level.OVERWORLD)) {
			thiz.setTransformer(new WorldTransformer(WrappingSettings.getXChunkBoundMin(), WrappingSettings.getZChunkBoundMin(), WrappingSettings.getXChunkBoundMax(), WrappingSettings.getZChunkBoundMax(), WrappingSettings.getXShift(),  WrappingSettings.getZShift()));
		}
		//else if(dimension.equals(Level.NETHER)) {
		//}
		else {
			thiz.setTransformer(WorldTransformer.INVALID);
		}
	}
}
