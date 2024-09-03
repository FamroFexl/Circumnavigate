package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.util.ServerTransformer;
import com.fexl.circumnavigate.util.WorldTransformer;
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
	ServerLevel thiz = (ServerLevel) (Object) this;

	/**
	 * Set the wrapping settings for each level when it is created
	 */
	@Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)V", at = @At("TAIL"))
	public void init(MinecraftServer server, Executor dispatcher, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey dimension, LevelStem levelStem, ChunkProgressListener progressListener, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime, RandomSequences randomSequences, CallbackInfo ci) {
		if(dimension.equals(Level.OVERWORLD)) {
			thiz.setTransformer(new WorldTransformer(WrappingSettings.getXChunkBoundMin(), WrappingSettings.getXChunkBoundMax(), WrappingSettings.getZChunkBoundMin(), WrappingSettings.getZChunkBoundMax(), WrappingSettings.getXShift(),  WrappingSettings.getZShift()));
		}
		//else if(dimension.equals(Level.NETHER)) {
			//thiz.setTransformer(new WorldTransformer(WrappingSettings.getXChunkBoundMin()/8, WrappingSettings.getXChunkBoundMax()/8, WrappingSettings.getZChunkBoundMin()/8, WrappingSettings.getZChunkBoundMax()/8, WrappingSettings.getXShift(),  WrappingSettings.getZShift()));
		//}
		else {
			thiz.setTransformer(new WorldTransformer(WrappingSettings.invalidPos));
		}
	}
}
