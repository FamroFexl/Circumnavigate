package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.core.WrappedChunks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin {

    @Shadow protected abstract void updateChunkForced(ChunkPos pos, boolean add);
    @Shadow private int simulationDistance;

    // those methods are called when player leaves/enters section, yet to further investigation.
    @WrapMethod(method = "addPlayer")
    private void addPlayerWrap(SectionPos sectionPos, ServerPlayer player, Operation<Void> original) {
        original.call(sectionPos, player);
        updateWrappedChunks(player);
    }

    @WrapMethod(method = "removePlayer")
    private void removePlayerWrap(SectionPos sectionPos, ServerPlayer player, Operation<Void> original) {
        original.call(sectionPos, player);
        updateWrappedChunks(player);
    }

    // Fixes #10
    @Unique
    private void updateWrappedChunks(ServerPlayer player) {
        WrappedChunks wrappedChunks = player.level().getWrappedChunks();
        WrappedChunks.ChunkLoad chunkLoad = wrappedChunks.playerMoved(player, simulationDistance);

        for (long chunk : chunkLoad.chunksToLoad()) {
            updateChunkForced(new ChunkPos(chunk), true);
        }

        for (long chunk : chunkLoad.chunksToUnload()) {
            updateChunkForced(new ChunkPos(chunk), false);
        }
    }
}