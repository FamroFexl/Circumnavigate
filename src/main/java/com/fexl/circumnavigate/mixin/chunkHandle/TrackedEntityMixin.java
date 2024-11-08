/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ChunkMap.TrackedEntity.class)
public abstract class TrackedEntityMixin {
	/**
	 * Updates an entity's player tracking based on the player's distance. Modified to support wrapped distances.
	 */
	@Redirect(method = "updatePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
	public Vec3 unwrapVec(Vec3 playerPos, Vec3 entityPos, @Local(argsOnly = true) ServerPlayer player) {
		return playerPos.subtract(player.level().getTransformer().translateVecFromBounds(playerPos, entityPos));
	}

}
