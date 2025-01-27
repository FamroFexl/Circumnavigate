/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.storage;

import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for context propagation down call stacks.
 */
public class TransformerRequests {
	public static DimensionTransformer chunkMapTransformer;
	public static MinecraftServer server = null;
	public static ServerLevel noiseLevel;
	public static List<ChunkAccess> structureChunks = new ArrayList<>();
}
