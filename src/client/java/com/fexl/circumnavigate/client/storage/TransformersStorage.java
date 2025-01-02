/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.client.storage;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Stores a list of level and transformer pairs, corresponding to the levels/dimensions on the server.
 */
public class TransformersStorage {
	private static HashMap<ResourceKey<Level>, WorldTransformer> transformers = new HashMap<>();

	/**
	 * Returns the transformer for the requested level/dimension
	 */
	public static WorldTransformer getTransformer(ResourceKey<Level> levelKey) {
		//No transformer packet received. No wrapping on server.
		WorldTransformer transformer = transformers.get(levelKey);
		if(transformer == null) {
			return new WorldTransformer(WorldTransformer.invalidPos, true);
		}
		return transformer;
	}

	public static void setTransformer(ResourceKey<Level> levelKey, WorldTransformer transformer) {
		transformers.put(levelKey, transformer);
	}

	public static void clearTransformers() {
		transformers.clear();
	}
}
