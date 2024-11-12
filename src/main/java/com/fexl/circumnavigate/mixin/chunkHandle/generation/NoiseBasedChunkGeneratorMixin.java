package com.fexl.circumnavigate.mixin.chunkHandle.generation;

import com.fexl.circumnavigate.accessors.LevelAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancel if beyond chunk bounds.
 */
@SuppressWarnings("deprecation")
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Unique ServerLevel level;
    @Unique ChunkPos chunkPos;

    @Inject(method = "applyCarvers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;setLargeFeatureSeed(JII)V"))
    public void carveOverBoundsCancel(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step, CallbackInfo ci, @Local ConfiguredWorldCarver<?> configuredWorldCarver) {
        this.level = level.getLevel();
        this.chunkPos = chunk.getPos();
        ((LevelAccessor) (Object) configuredWorldCarver).setLevel(level.getLevel());
    }

    @WrapOperation(method = "applyCarvers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Lnet/minecraft/util/RandomSource;)Z"))
    public boolean carveOverBoundsCancel2(ConfiguredWorldCarver<?> instance, RandomSource random, Operation<Boolean> original) {
        if(level.getTransformer().isChunkOverBounds(chunkPos)) return false;
        return original.call(instance, random);
    }

    @Inject(method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V", at = @At("HEAD"), cancellable = true)
    public void surfaceOverBoundsCancel(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk, CallbackInfo ci) {
        if(level.getLevel().getTransformer().isChunkOverBounds(chunk.getPos())) ci.cancel();
    }

    @WrapOperation(method = "doFill", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;debugVoidTerrain(Lnet/minecraft/world/level/ChunkPos;)Z"))
    public boolean chunkPosOverBoundsCancel(ChunkPos chunkPos, Operation<Boolean> original) {
        ServerLevel level = ((LevelAccessor) this).getLevel();

        if(level.getTransformer().isChunkOverBounds(chunkPos)) return true;
        return original.call(chunkPos);
    }

}
