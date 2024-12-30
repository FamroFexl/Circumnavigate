/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
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

import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow public abstract MinecraftServer getServer();
	@Shadow public abstract void broadcastAll(Packet<?> packet);
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
	 * Modifies the server's per-level view distance to align with client expectations.
	 */
	@Inject(method = "setViewDistance", at = @At("HEAD"), cancellable = true)
	public void setViewDistance(int viewDistance, CallbackInfo ci) {
		ci.cancel();

		this.viewDistance = viewDistance;
		this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(viewDistance));

		for (ServerLevel serverLevel : this.getServer().getAllLevels()) {
			if (serverLevel != null) {
				WorldTransformer levelTransformer = serverLevel.getTransformer();
				serverLevel.getChunkSource().setViewDistance(levelTransformer.limitViewDistance(viewDistance));

			}
		}
	}

	/**
	 * Modifies the server's per-level simulation distance to align with client expectations.
	 */
	@Inject(method = "setSimulationDistance", at = @At("HEAD"), cancellable = true)
	public void setSimulationDistance(int simulationDistance, CallbackInfo ci) {
		ci.cancel();

		this.simulationDistance = simulationDistance;
		this.broadcastAll(new ClientboundSetSimulationDistancePacket(simulationDistance));

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
