/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
	@Shadow protected abstract DimensionTransition getExitPortal(ServerLevel level, Entity entity, BlockPos pos, BlockPos exitPos, boolean isNether, WorldBorder worldBorder);

	/**
	 * Adjust portal scaling to dimension scaling.
	 */
	@Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
	public void getPortalDestination(ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<DimensionTransition> cir) {
		ResourceKey<Level> resourceKey = level.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
		ServerLevel serverLevel = level.getServer().getLevel(resourceKey);
		if (serverLevel == null) {
			cir.setReturnValue(null);
		} else {
			boolean bl = serverLevel.dimension() == Level.NETHER;
			WorldBorder worldBorder = serverLevel.getWorldBorder();
			double x = ((double) serverLevel.getTransformer().xWidth) / ((double) level.getTransformer().xWidth);
			double z = ((double) serverLevel.getTransformer().zWidth) / ((double) level.getTransformer().zWidth);
			BlockPos blockPos = worldBorder.clampToBounds(entity.getX() * x, entity.getY(), entity.getZ() * z);
			cir.setReturnValue(this.getExitPortal(serverLevel, entity, pos, blockPos, bl, worldBorder));
		}
	}
}
