/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VibrationSystem.Listener.class)
public abstract class VibrationSystemMixin {
	/**
	 * Wrap distance calculation for vibration travel distance.
	 */
	@ModifyArg(method = "scheduleVibration", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gameevent/vibrations/VibrationInfo;<init>(Lnet/minecraft/core/Holder;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;)V"))
	public float schedule(float distance, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true, ordinal = 0) Vec3 pos, @Local(argsOnly = true, ordinal = 1) Vec3 sensorPos) {
		return Mth.sqrt((float)level.getTransformer().Vector3D.sqrDistToBounds(pos, sensorPos));
	}
}
