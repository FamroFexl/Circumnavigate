/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.network.packet.DimensionWrappingPayload;
import com.fexl.circumnavigate.options.DimensionWrappingSettings;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Circumnavigate implements ModInitializer {
	public static final String MOD_ID = "circumnavigate";
	public static final Logger LOGGER = LogManager.getLogger(StringUtils.capitalize(MOD_ID));

	public static final boolean DEV_MODE = System.getProperty("env", "").equals("dev");

	public static int tickCount = 0;
	@Override
	public void onInitialize() {
		DimensionWrappingSettings settings = new DimensionWrappingSettings(-16, 16, -16, 16, DimensionWrappingSettings.Axis.X, 0, false);
		DimensionTransformer test = new DimensionTransformer(settings, false);

		RandomSource rand = RandomSource.create();
		long nanoIn = System.nanoTime();
		for(int i = 0; i < 10000; i++) {
			test.Coord.X.wrap(rand.nextDouble());
		}
		long nanoOut = System.nanoTime();
		System.out.println("Time to wrap: " + (nanoOut - nanoIn));

		nanoIn = System.nanoTime();
		for(int i = 0; i < 10000; i++) {
			test.Coord.X.wrap(rand.nextInt());
		}
		nanoOut = System.nanoTime();
		System.out.println("Time to wrap int: " + (nanoOut - nanoIn));

		nanoIn = System.nanoTime();
		for(int i = 0; i < 10000; i++) {
			test.Coord.X.unwrap(rand.nextDouble(), rand.nextDouble());
		}
		nanoOut = System.nanoTime();
		System.out.println("Time to unwrap: " + (nanoOut - nanoIn));

		nanoIn = System.nanoTime();
		for(int i = 0; i < 10000; i++) {
			test.Coord.X.unwrap(rand.nextInt(), rand.nextInt());
		}
		nanoOut = System.nanoTime();
		System.out.println("Time to unwrap int: " + (nanoOut - nanoIn));
		PayloadTypeRegistry.configurationS2C().register(DimensionWrappingPayload.TYPE, DimensionWrappingPayload.STREAM_CODEC);
		//PayloadTypeRegistry.playS2C().register(ChunkLoadingLevelsPayload.TYPE, ChunkLoadingLevelsPayload.STREAM_CODEC);

		StructurePiece originalPiece;
		/**
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
