/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.injected;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public interface ServerPlayerInjector {
	default double getClientX() { return 0; }
	default double getClientZ() { return 0; }

	default ChunkPos getClientChunk() { return ChunkPos.ZERO; }

	default BlockPos getClientBlock() { return BlockPos.ZERO; }

	default Vec3 getClientPosition() { return Vec3.ZERO; }

	default void setClientX(double clientX) {};
	default void setClientZ(double clientZ) {};
}
