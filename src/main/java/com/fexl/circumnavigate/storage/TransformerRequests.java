/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.storage;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Storage for context propagation down call stacks.
 */
public class TransformerRequests {
	public static ServerLevel chunkMapLevel;
	public static ServerLevel chunkCacheLevel;
}
