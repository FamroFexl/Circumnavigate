package com.fexl.circumnavigate.core;

import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

// TODO: Optimize, utilize more methods
// This class should be thread safe and is used to keep track of which chunks are currently wrapped by players.
public class WrappedChunks {

    private final Map<Long, Set<ServerPlayer>> wrappedLoadedChunks; // uses long for easier comparison - less compute
    public record ChunkLoad(Set<Long> chunksToLoad, Set<Long> chunksToUnload) {}

    public WrappedChunks() {
        this.wrappedLoadedChunks = new ConcurrentHashMap<>();
    }

    public boolean isPlayerInChunk(Long chunkPos) {
        return wrappedLoadedChunks.containsKey(chunkPos) && !wrappedLoadedChunks.get(chunkPos).isEmpty();
    }

    public void loadChunk(ServerPlayer player, Long chunkPos, int ticketLevel) {
        wrappedLoadedChunks.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet()).add(player);
    }

    public void unloadChunk(ServerPlayer player, Long chunkPos) {
        wrappedLoadedChunks.computeIfPresent(chunkPos, (k, players) -> {
            players.remove(player);
            return players.isEmpty() ? null : players;
        });
    }

    // should be called when player moves to other chunk / player connects
    // returns a pair of list of chunks which should be loaded and list of chunks which can be unloaded due to player movement
    public ChunkLoad playerMoved(@NotNull ServerPlayer player, int simulationDistance) {
        Set<Long> chunksToUnload = ConcurrentHashMap.newKeySet();
        Set<Long> chunksToLoad = ConcurrentHashMap.newKeySet();
        Set<Long> occupiedChunks = getOccupiedChunks(player, simulationDistance);

        // check which chunks should be loaded
        occupiedChunks.stream()
            .filter(chunkPos -> !wrappedLoadedChunks.containsKey(chunkPos))
            .forEach(chunksToLoad::add);

        // check which chunks should be unloaded
        wrappedLoadedChunks.entrySet().stream()
            .filter(entry -> !occupiedChunks.contains(entry.getKey()) && entry.getValue().size() == 1 && entry.getValue().contains(player))
            .forEach(entry -> chunksToUnload.add(entry.getKey()));

        return new ChunkLoad(chunksToLoad, chunksToUnload);
    }

    // should be called on player disconnect
    public Set<Long> unloadAllPlayerChunks(@NotNull ServerPlayer player) {
        Set<Long> chunksToUnload = new CopyOnWriteArraySet<>();
        wrappedLoadedChunks.entrySet().stream()
            .filter(entry -> entry.getValue().contains(player) && entry.getValue().size() == 1)
            .forEach(entry -> chunksToUnload.add(entry.getKey()));
        return chunksToUnload;
    }

    public Set<Long> getOccupiedChunks(@NotNull ServerPlayer player, int simulationDistance) {
        ChunkPos chunkPos = player.chunkPosition();
        WorldTransformer transformer = player.level().getTransformer();
        ChunkPos wrappedChunkPos = transformer.translateChunkToBounds(chunkPos);

        Map<Integer, Set<Long>> chunksPriority = new ConcurrentHashMap<>();

        for (int i = 0; i <= simulationDistance; i++) {
            int ticketLevel = getPlayerTicketLevel(i);
            chunksPriority.putIfAbsent(ticketLevel, ConcurrentHashMap.newKeySet());

            for (int x = -i; x <= i; x++) {
                for (int z = -i; z <= i; z++) {
                    ChunkPos currentChunkPos = new ChunkPos(wrappedChunkPos.x + x, wrappedChunkPos.z + z);
                    ChunkPos wrappedCurrentChunkPos = transformer.translateChunkToBounds(currentChunkPos);
                    long chunkPosLong = wrappedCurrentChunkPos.toLong();
                    chunksPriority.get(ticketLevel).add(chunkPosLong);
                }
            }
        }

        Set<Long> occupiedChunks = new CopyOnWriteArraySet<>();
        for (int i = 31; i >= 0; i--) {
            if (chunksPriority.containsKey(i)) {
                occupiedChunks.addAll(chunksPriority.get(i));
            }
        }

        return occupiedChunks;
    }

    /**
     * Determines the player ticket level based on the simulation distance.
     * If the ticket level is 31 or lower, all ticks are processed.
     * Took from {@link net.minecraft.server.level.DistanceManager#getPlayerTicketLevel()}
     *
     * @param simulationDistance the distance for simulation
     * @return the player ticket level
     * @see <a href="https://minecraft.wiki/w/Chunk#Level_and_load_type">Chunk Level and Load Type</a>
     */
    private int getPlayerTicketLevel(int simulationDistance) {
        return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - simulationDistance);
    }
}
