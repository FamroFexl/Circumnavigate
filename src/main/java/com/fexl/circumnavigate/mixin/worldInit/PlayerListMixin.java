/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.network.packet.LevelWrappingPayload;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow public abstract List<ServerPlayer> getPlayers();
	@Shadow public abstract MinecraftServer getServer();
	@Shadow private int viewDistance;
	@Shadow private int simulationDistance;

	/**
	 * Initializes the player's client-side positioning so they can be used for unwrapping operations.
	 */
	@Inject(method = "placeNewPlayer", at = @At("TAIL"))
	public void placeNewPlayer2(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		player.setClientX(player.getX());
		player.setClientZ(player.getZ());
	}

	/**
	 * Modifies the client view distance so it is within world bounds and chunk loading requirements.
	 */
	@Inject(method = "setViewDistance", at = @At("HEAD"), cancellable = true)
	public void setViewDistance(int viewDistance, CallbackInfo ci) {
		ci.cancel();

		this.viewDistance = viewDistance;
		for (ServerPlayer player : this.getPlayers()) {
			WorldTransformer transformer = player.level().getTransformer();
			player.connection.send(new ClientboundSetChunkCacheRadiusPacket(transformer.limitViewDistance(viewDistance)));
		}

		for (ServerLevel serverLevel : this.getServer().getAllLevels()) {
			if (serverLevel != null) {
				WorldTransformer levelTransformer = serverLevel.getTransformer();
				serverLevel.getChunkSource().setViewDistance(levelTransformer.limitViewDistance(viewDistance));

			}
		}
	}

	@Inject(method = "setSimulationDistance", at = @At("HEAD"), cancellable = true)
	public void setSimulationDistance(int simulationDistance, CallbackInfo ci) {
		ci.cancel();

		this.simulationDistance = simulationDistance;
		for (ServerPlayer player : this.getPlayers()) {
			WorldTransformer transformer = player.level().getTransformer();
			player.connection.send(new ClientboundSetSimulationDistancePacket(transformer.limitViewDistance(simulationDistance)));
		}

		for (ServerLevel serverLevel : this.getServer().getAllLevels()) {
			if (serverLevel != null) {
				WorldTransformer levelTransformer = serverLevel.getTransformer();
				serverLevel.getChunkSource().setSimulationDistance(levelTransformer.limitViewDistance(simulationDistance));
			}
		}
	}

	/**
	 * Wraps the X for the ServerPlayer so it can apply to them
	 */
	@Redirect(method = "broadcast", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getX()D"))
	public double broadcast_X(ServerPlayer instance, @Local(ordinal = 0, argsOnly = true) double x) {
		return instance.serverLevel().getTransformer().Coord.X.unwrapFromBounds(x, instance.getX());
	}

	/**
	 * Wraps the Z for the ServerPlayer so it can apply to them
	 */
	@Redirect(method = "broadcast", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getZ()D"))
	public double broadcast_Z(ServerPlayer instance, @Local(ordinal = 2, argsOnly = true) double z) {
		return instance.serverLevel().getTransformer().Coord.Z.unwrapFromBounds(z, instance.getZ());
	}
}
