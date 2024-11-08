/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.blockHandle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public class ExplosionMixin {
	@Final @Shadow private Level level;
	@Final @Shadow private double x;
	@Final @Shadow private double z;

	@Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"))
	public BlockPos wrapBlockPos(double x, double y, double z) {
		if(level.isClientSide) return BlockPos.containing(x, y, z);
		return level.getTransformer().translateBlockToBounds(BlockPos.containing(x, y, z));
	}

	@Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"))
	public double unwrapEntityX(Entity instance) {
		if(level.isClientSide) return instance.getX();
		return level.getTransformer().xTransformer.unwrapCoordFromLimit(instance.getX(), x);
	}

	@Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D"))
	public double unwrapEntityZ(Entity instance) {
		if(level.isClientSide) return instance.getZ();
		return level.getTransformer().zTransformer.unwrapCoordFromLimit(instance.getZ(), z);
	}
}
