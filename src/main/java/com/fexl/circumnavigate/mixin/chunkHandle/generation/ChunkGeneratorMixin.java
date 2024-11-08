package com.fexl.circumnavigate.mixin.chunkHandle.generation;

import com.fexl.circumnavigate.accessors.LevelAccessor;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements LevelAccessor {
	/**
	 * Cancel if beyond chunk bounds.
	 */
    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
    public void decorateOverBoundsCancel(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {
        this.setLevel(level.getLevel());

        WorldTransformer transformer = level.getLevel().getTransformer();

        if(transformer.isChunkOverBounds(chunk.getPos())) ci.cancel();
    }

    @Unique
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
