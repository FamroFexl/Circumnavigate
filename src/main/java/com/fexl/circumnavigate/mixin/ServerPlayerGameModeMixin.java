/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin;

import com.fexl.circumnavigate.util.ServerTransformer;
import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow ServerLevel level;

	@Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 0))
	public double wrapInteractionDistanceCheck(Vec3 instance, Vec3 vec) {
		WorldTransformer transformer = level.getTransformer();
		return transformer.distanceToSqrWrapped(instance, vec);
	}
}
