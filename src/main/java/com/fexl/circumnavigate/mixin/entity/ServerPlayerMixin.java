/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
		return this.serverLevel().getTransformer().Block.unwrap(thiz.blockPosition(), blockPos);
	}
}
