package com.fexl.circumnavigate.mixin.chunkHandle.generation;

import com.fexl.circumnavigate.accessors.LevelAccessor;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(ConfiguredWorldCarver.class)
public class ConfiguredWorldCarverMixin implements LevelAccessor {
	/**
	 * Cancel if beyond chunk bounds.
	 */
    @Inject(method = "carve", at = @At("HEAD"), cancellable = true)
    public void carveOverBoundsCancel(CarvingContext context, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random, Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask, CallbackInfoReturnable<Boolean> cir) {
        WorldTransformer transformer = getLevel().getTransformer();
        if(transformer.isChunkOverBounds(chunk.getPos())) cir.setReturnValue(false);
    }

    ServerLevel level;

    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(ServerLevel level) {
        this.level = level;
    }
}
