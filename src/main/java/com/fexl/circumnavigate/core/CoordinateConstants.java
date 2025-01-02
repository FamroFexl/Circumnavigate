package com.fexl.circumnavigate.core;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class CoordinateConstants {
    /** The width of a single chunk. */
    public static final int CHUNK_WIDTH = LevelChunkSection.SECTION_WIDTH;

    /** A value well-beyond vanilla chunk limits used to disable WorldTransformers. */
    public static final int DISABLING_CHUNK_POS = ChunkPos.getX(ChunkPos.INVALID_CHUNK_POS)+10000;

    /** The minimum chunk buffer required around a view distance when constrained to the level size. */
    public static final int MIN_VIEW_DISTANCE_BUFFER = 3;

    /** The minimum level size which still allows the minimum default 2-chunk view distance. */
    public static final int MIN_LEVEL_WIDTH = 10;

}
