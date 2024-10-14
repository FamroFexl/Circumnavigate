/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.server.level.ChunkMap;
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

@Mixin(ChunkMap.TrackedEntity.class)
public abstract class TrackedEntityMixin {
	@Shadow Entity entity;

	@Shadow abstract int getEffectiveRange();

	@Shadow Set<ServerPlayerConnection> seenBy;

	@Shadow ServerEntity serverEntity;


	@Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
	public void updatePlayer(ServerPlayer player, CallbackInfo ci) {
		WorldTransformer transformer = player.serverLevel().getTransformer();
		ci.cancel();
		if (player != this.entity) {
			//Vec3 vec3 = player.position().subtract(this.entity.position());
			//Needs both the player and the entity position unwrapped relative to the client
			Vec3 vec3 = player.position().subtract(transformer.translateVecFromBounds(player.position(), this.entity.position()));
			int i = player.serverLevel().getChunkSource().chunkMap.getPlayerViewDistance(player);
			double d = (double)Math.min(this.getEffectiveRange(), i * 16);
			double e = vec3.x * vec3.x + vec3.z * vec3.z;
			double f = d * d;
			boolean bl = e <= f
				&& this.entity.broadcastToPlayer(player)
				&& player.serverLevel().getChunkSource().chunkMap.isChunkTracked(player, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
			//boolean bl = e <= f
				//&& this.entity.broadcastToPlayer(player)
				//&& player.serverLevel().getChunkSource().chunkMap.isChunkTracked(player, transformer.xTransformer.unwrapChunkFromLimit(player.getClientChunk().x, this.entity.chunkPosition().x), transformer.zTransformer.unwrapChunkFromLimit(player.getClientChunk().z, this.entity.chunkPosition().z));
			if (bl) {
				if (this.seenBy.add(player.connection)) {
					if(serverEntity.entity instanceof ServerPlayer entityPlayer) {
						//System.out.println(player.getName().getString() + " added " + entityPlayer);
						//System.out.println((e <= f) + " " + this.entity.broadcastToPlayer(player) + " " + player.serverLevel().getChunkSource().chunkMap.isChunkTracked(player, this.entity.chunkPosition().x, this.entity.chunkPosition().z) + ", " + this.entity.chunkPosition());
					}
					this.serverEntity.addPairing(player);
				}
			} else if (this.seenBy.remove(player.connection)) {
				if(serverEntity.entity instanceof ServerPlayer entityPlayer) {
					//System.out.println(player.getName().getString() + " removed " + entityPlayer);
					//System.out.println((e <= f) + " " + this.entity.broadcastToPlayer(player) + " " + player.serverLevel().getChunkSource().chunkMap.isChunkTracked(player, this.entity.chunkPosition().x, this.entity.chunkPosition().z) + ", " + this.entity.chunkPosition());
				}
				this.serverEntity.removePairing(player);
			}
		}
	}

}
