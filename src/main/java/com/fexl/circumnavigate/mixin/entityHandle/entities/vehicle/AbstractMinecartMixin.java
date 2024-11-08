/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle.entities.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {
	@Unique
	AbstractMinecart thiz = (AbstractMinecart) (Object) this;

	@ModifyVariable(method = "moveAlongTrack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;setPos(DDD)V", shift = At.Shift.AFTER), index = 1, argsOnly = true)
	public BlockPos moveAlongTrack(BlockPos blockPos) {
		if(thiz.level().isClientSide) return blockPos;
		return thiz.level().getTransformer().translateBlockFromBounds(thiz.blockPosition(), blockPos);
	}
}
