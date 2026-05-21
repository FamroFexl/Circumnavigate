/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.network.packet.DimensionWrappingPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Circumnavigate implements ModInitializer {
	public static final String MOD_ID = "circumnavigate";
	public static final Logger LOGGER = LogManager.getLogger(StringUtils.capitalize(MOD_ID));

	public static final boolean DEV_MODE = System.getProperty("env", "").equals("dev");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationS2C().register(DimensionWrappingPayload.TYPE, DimensionWrappingPayload.STREAM_CODEC);

		/**
		PayloadTypeRegistry.playS2C().register(ChunkLoadingLevelsPayload.TYPE, ChunkLoadingLevelsPayload.STREAM_CODEC);

		ServerTickEvents.END_SERVER_TICK.register((server -> {
			if (tickCount++ >= 10) {
				server.getAllLevels().forEach((serverLevel -> {
					for(ServerPlayer player : serverLevel.players()) {
						ServerPlayNetworking.send(player, new ChunkLoadingLevelsPayload(DebugInfo.chunkLoadingLevels));
					}
				}));
				DebugInfo.chunkLoadingLevels = new HashMap<>();
				tickCount = 0;

//				LOGGER.info("Sent chunk loading levels to all players");
			}
		}));**/
	}
}
