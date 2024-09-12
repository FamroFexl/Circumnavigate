/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import com.fexl.circumnavigate.network.packet.ClientboundWrappingDataPacket;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class CircumnavigateClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		//Read incoming transformers from the server. Received during server login.
		ClientPlayNetworking.registerGlobalReceiver(ClientboundWrappingDataPacket.WRAPPING_DATA, (client, handler, buf, responseSender) -> {
			HashMap<ResourceKey<Level>, WorldTransformer> transformers = new HashMap<>();
			int size = buf.readInt();
			for(int i = 0; i < size; i++) {
				ResourceKey<Level> levelKey = buf.readResourceKey(Registries.DIMENSION);
				WorldTransformer transformer = new WorldTransformer(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
				transformers.put(levelKey, transformer);
			}

			TransformersStorage.setTransformers(transformers);
		});
	}
}
