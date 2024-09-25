/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunkHandle;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityMixin {
	@Shadow Entity entity;

	@Shadow abstract int getEffectiveRange();

	@Shadow Set<ServerPlayerConnection> seenBy;

	@Shadow ServerEntity serverEntity;

	@Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
	public void updatePlayer(ServerPlayer player, CallbackInfo ci) {
		ci.cancel();
		if (player != this.entity) {
			//Vec3 vec3 = player.position().subtract(this.entity.position());
			//Needs both the player and the entity position unwrapped relative to the client
			Vec3 vec3 = player.getClientPosition().subtract(player.level().getTransformer().translateVecFromBounds(player.getClientPosition(), this.entity.position()));
			int i = player.serverLevel().getChunkSource().chunkMap.getPlayerViewDistance(player);
			double d = (double)Math.min(this.getEffectiveRange(), i * 16);
			double e = vec3.x * vec3.x + vec3.z * vec3.z;
			double f = d * d;
			boolean bl = e <= f
				&& this.entity.broadcastToPlayer(player)
				&& player.serverLevel().getChunkSource().chunkMap.isChunkTracked(player, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
			if (bl) {
				if (this.seenBy.add(player.connection)) {
					this.serverEntity.addPairing(player);
				}
			} else if (this.seenBy.remove(player.connection)) {
				this.serverEntity.removePairing(player);
			}
		}
	}

}
