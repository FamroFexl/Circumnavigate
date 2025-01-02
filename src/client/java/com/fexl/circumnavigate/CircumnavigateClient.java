/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.network.packet.ChunkLoadingLevelsPayload;
import com.fexl.circumnavigate.network.packet.LevelWrappingPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.HashMap;

public class CircumnavigateClient implements ClientModInitializer {
	public static HashMap<ChunkPos, Integer> chunkLoadingLevels = new HashMap<>();

	@Override
	public void onInitializeClient() {
		//Read an incoming transformer from the server. Received during server configuration.
		ClientConfigurationNetworking.registerGlobalReceiver(LevelWrappingPayload.TYPE, (((payload, context) -> {
			TransformersStorage.setTransformer(payload.levelKey(), new WorldTransformer(payload.wrappingSettings(), true));
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
