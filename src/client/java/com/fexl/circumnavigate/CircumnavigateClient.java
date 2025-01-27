/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.network.packet.DimensionWrappingPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.ChunkPos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import static com.fexl.circumnavigate.Circumnavigate.LOGGER;

public class CircumnavigateClient implements ClientModInitializer {
	public static HashMap<ChunkPos, Integer> chunkLoadingLevels = new HashMap<>();

	@Override
	public void onInitializeClient() {
		//Read an incoming transformer from the server. Received during server configuration.
		ClientConfigurationNetworking.registerGlobalReceiver(DimensionWrappingPayload.TYPE, (((payload, context) -> {
			TransformersStorage.setTransformer(payload.levelKey(), new DimensionTransformer(payload.wrappingSettings(), true));
		})));

		copyShader();

		/**
		ClientPlayNetworking.registerGlobalReceiver(ChunkLoadingLevelsPayload.TYPE, (((payload, context) -> {
			HashMap<ChunkPos, Integer> newLevels = payload.getLevels();
			for(ChunkPos serverPos : newLevels.keySet()) {
				chunkLoadingLevels.put(serverPos, newLevels.get(serverPos));
			}
		})));**/
	}

	private void copyShader() {
		//Check if Iris is installed
		if(!FabricLoader.getInstance().isModLoaded("iris")) return;

		String shaderName = "Circumnavigate Curvature Shader.zip";

		Path shaderDir = FabricLoader.getInstance().getGameDir().resolve("shaderpacks");
		Path shaderPath = shaderDir.resolve(shaderName);

		//Only copy the shader over if it doesn't exist
		if(shaderPath.toFile().exists()) {
			LOGGER.info("Default shader already exists in \"shaderpacks\", skipping...");
			return;
		}

		//Create "shaderpacks" directory if it doesn't exist
		try {
			if(!Files.exists(shaderDir)) Files.createDirectory(shaderDir);
		} catch (IOException e) {

			LOGGER.error("Couldn't create a \"shaderpacks\" folder!");
		}

		//Copy default shader if it doesn't exist.
		try (InputStream in = getClass().getResourceAsStream("/" + shaderName)){
			Files.copy(in, shaderPath, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.info("Copied default shader to \"shaderpacks\"!");
		} catch (IOException e) {
			LOGGER.error("Couldn't copy the default shader to \"shaderpacks\"!");
		}

	}
}
