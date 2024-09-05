/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.client.storage;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;

/**
 * Stores a list of level and transformer pairs, corresponding to the levels/dimensions on the server.
 */
public class TransformersStorage {
	private static HashMap<ResourceKey<Level>, WorldTransformer> transformers = null;

	/**
	 * Returns the transformer for the requested level/dimension
	 */
	public static WorldTransformer getTransformer(ResourceKey<Level> levelKey) {
		//No transformer packet received. No wrapping on server.
		if(transformers == null) {
			return new WorldTransformer(WrappingSettings.invalidPos);
		}
		return transformers.get(levelKey);
	}

	public static void setTransformers(HashMap<ResourceKey<Level>, WorldTransformer> transformers) {
		TransformersStorage.transformers = transformers;
	}
}
