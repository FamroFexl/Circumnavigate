/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.network.packet.DimensionWrappingPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;

public class CircumnavigateClient implements ClientModInitializer {
	public static HashMap<ChunkPos, Integer> chunkLoadingLevels = new HashMap<>();

	@Override
	public void onInitializeClient() {
		//Read an incoming transformer from the server. Received during server configuration.
		ClientConfigurationNetworking.registerGlobalReceiver(DimensionWrappingPayload.TYPE, (((payload, context) -> {
			TransformersStorage.setTransformer(payload.levelKey(), new DimensionTransformer(payload.wrappingSettings(), true));
		})));

		/**
		ClientPlayNetworking.registerGlobalReceiver(ChunkLoadingLevelsPayload.TYPE, (((payload, context) -> {
			HashMap<ChunkPos, Integer> newLevels = payload.getLevels();
			for(ChunkPos serverPos : newLevels.keySet()) {
				chunkLoadingLevels.put(serverPos, newLevels.get(serverPos));
			}
		})));**/
	}
}
