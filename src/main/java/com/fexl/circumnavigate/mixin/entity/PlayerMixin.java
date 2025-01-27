/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entity;

import com.fexl.circumnavigate.processing.AABBWrapped;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {
	Player thiz = (Player) (Object) this;
	@Redirect(method = "canInteractWithEntity(Lnet/minecraft/world/phys/AABB;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
	public double canInteractWithEntity(AABB instance, Vec3 vec) {
		return new AABBWrapped(instance, thiz.level().getTransformer().SSO()).distanceToSqr(vec);
	}

	@Redirect(method = "canInteractWithBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
	public double canInteractWithBlock(AABB instance, Vec3 vec) {
		return new AABBWrapped(instance, thiz.level().getTransformer().SSO()).distanceToSqr(vec);
	}
}
