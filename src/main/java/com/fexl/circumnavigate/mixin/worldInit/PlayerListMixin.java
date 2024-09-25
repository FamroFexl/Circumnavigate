/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.network.packet.ClientboundWrappingDataPacket;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	PlayerList thiz = (PlayerList) (Object) this;

	PlayerListAccessorMixin accessor = (PlayerListAccessorMixin) thiz;
	/**
	 * Send a new player the wrappingSettings for the world.
	 */
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	public void placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		HashMap<ResourceKey<Level>, WorldTransformer> transformers = new HashMap<>();
		for(ServerLevel level : thiz.getServer().getAllLevels()) {
			transformers.put(level.dimension(), level.getTransformer());
		}

		ClientboundWrappingDataPacket.send(player, transformers);
	}

	@Inject(method = "placeNewPlayer", at = @At("TAIL"))
	public void placeNewPlayer2(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		player.setClientX(player.getX());
		player.setClientZ(player.getZ());
	}

	/**
	 * Modifies the client view distance so it is within world bounds and chunk loading requirements.
	 */
	@Inject(method = "setViewDistance", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void setViewDistance(int viewDistance, CallbackInfo ci) {
		ci.cancel();

		accessor.setViewDistance(viewDistance);
		for (ServerPlayer player : accessor.getPlayers()) {
			WorldTransformer transformer = player.level().getTransformer();
			player.connection.send(new ClientboundSetChunkCacheRadiusPacket(transformer.limitViewDistance(viewDistance)));
		}

		for (ServerLevel serverLevel : accessor.getServer().getAllLevels()) {
			if (serverLevel != null) {
				WorldTransformer levelTransformer = serverLevel.getTransformer();
				serverLevel.getChunkSource().setViewDistance(levelTransformer.limitViewDistance(viewDistance));

			}
		}
	}
}
