/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathNavigationRegion.class)
public class PathNavigationRegionMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(Level level, BlockPos centerPos, BlockPos offsetPos, CallbackInfo ci) {
		PathNavigationRegion thiz = (PathNavigationRegion) (Object) this;

		if(level.isClientSide) thiz.setTransformer(WorldTransformer.INVALID);
		if(level instanceof ServerLevel serverLevel) thiz.setTransformer(serverLevel.getTransformer());
	}
}
