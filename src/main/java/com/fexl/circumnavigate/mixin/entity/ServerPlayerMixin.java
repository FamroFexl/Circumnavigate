/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	ServerPlayer thiz = (ServerPlayer) (Object) this;

	@Shadow
	public abstract ServerLevel serverLevel();

	/**
	 * Wrap bed block distance check.
	 */
	@ModifyVariable(method = "isReachableBedBlock", at = @At("HEAD"), index = 1, argsOnly = true)
	public BlockPos modifyBlockPos(BlockPos blockPos) {
		return this.serverLevel().getTransformer().Block.unwrapFromBounds(thiz.blockPosition(), blockPos);
	}

	@Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V"))
	public void changeDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
		thiz.setClientX(transition.pos().x);
		thiz.setClientZ(transition.pos().z);
	}
}
