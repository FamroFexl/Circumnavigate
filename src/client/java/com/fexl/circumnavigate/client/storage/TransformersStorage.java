/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.client.storage;

import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;

/**
 * Stores a list of level and transformer pairs, corresponding to the levels/dimensions on the server.
 */
public class TransformersStorage {
	private static HashMap<ResourceKey<Level>, DimensionTransformer> transformers = new HashMap<>();

	/**
	 * Returns the transformer for the requested level/dimension
	 */
	public static DimensionTransformer getTransformer(ResourceKey<Level> levelKey) {
		//No transformer packet received. No wrapping on server.
		DimensionTransformer transformer = transformers.get(levelKey);
		if(transformer == null) {
			return DimensionTransformer.DISABLED;
		}
		return transformer;
	}

	public static void setTransformer(ResourceKey<Level> levelKey, DimensionTransformer transformer) {
		transformers.put(levelKey, transformer);
	}

	public static void clearTransformers() {
		transformers.clear();
	}
}
