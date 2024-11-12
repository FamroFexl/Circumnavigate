/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.storage;

import net.minecraft.server.level.ServerLevel;

/**
 * Storage for context propagation down call stacks.
 */
public class TransformerRequests {
	public static ServerLevel chunkMapLevel;
	public static ServerLevel chunkCacheLevel;
}
