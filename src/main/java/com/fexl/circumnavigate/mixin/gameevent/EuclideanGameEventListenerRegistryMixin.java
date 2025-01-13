/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.gameevent;

import com.fexl.circumnavigate.processing.Vec3iWrapped;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.EuclideanGameEventListenerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(EuclideanGameEventListenerRegistry.class)
public class EuclideanGameEventListenerRegistryMixin {
	/**
	 * Wrap distance check for vibrations etc.
	 */
	@Redirect(method = "getPostableListenerPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D"))
	private static double modifySqr(BlockPos instance, Vec3i vec3i, @Local(argsOnly = true) ServerLevel level) {
		return new Vec3iWrapped(instance.getX(), instance.getY(), instance.getZ(), level.getTransformer().onlyServerSide()).distSqr(vec3i);
	}

}
