/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle.collisions;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	@Shadow List<? extends Player> players();

	/**
	 * Determines if two entity bounding boxes intersect. Modified to support wrapped worlds.
	 */
	@Redirect(method = "isUnobstructed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/Shapes;joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z"))
	default boolean create(VoxelShape shape1, VoxelShape shape2, BooleanOp resultOperator, @Local(argsOnly = true) @Nullable Entity entity) {
		EntityGetter thiz = (EntityGetter) (Object) this;

		if(this instanceof Level level && level.isClientSide) return Shapes.joinIsNotEmpty(shape1, shape2, resultOperator);

		WorldTransformer transformer = thiz.getTransformer();
		VoxelShape result = Shapes.create(transformer.translateAABBFromBounds(shape1.bounds(), shape2.bounds()));

		return Shapes.joinIsNotEmpty(shape1, result, resultOperator);
	}

	@Redirect(method = "getNearestPlayer(DDDDLjava/util/function/Predicate;)Lnet/minecraft/world/entity/player/Player;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(DDD)D"))
	default double nearestPlayerDistanceWrap(Player player, double x, double y, double z) {
		return wrapDistance(player, x, y, z);
	}

	@Redirect(method = "hasNearbyAlivePlayer(DDDD)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(DDD)D"))
	default double hasNearbyAlivePlayerDistanceWrap(Player player, double x, double y, double z) {
		return wrapDistance(player, x, y, z);
	}

	@Redirect(method = "getNearestEntity(Ljava/util/List;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;DDD)Lnet/minecraft/world/entity/LivingEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;distanceToSqr(DDD)D"))
	default double nearestEntityDistanceWrap(LivingEntity livingEntity, double x, double y, double z) {
		return wrapDistance(livingEntity, x, y, z);
	}

	@Inject(method = "getNearbyPlayers", at = @At("HEAD"), cancellable = true)
	default void getNearbyPlayers(TargetingConditions predicate, LivingEntity target, AABB area, CallbackInfoReturnable<List<Player>> cir) {
		List<Player> list = Lists.<Player>newArrayList();

		for (Player player : this.players()) {
			WorldTransformer transformer = player.level().getTransformer();
			if (area.contains(transformer.xTransformer.unwrapCoordFromLimit(area.minX, player.getX()), player.getY(), transformer.zTransformer.unwrapCoordFromLimit(area.minZ, player.getZ())) && predicate.test(target, player)) {
				list.add(player);
			}
		}

		cir.setReturnValue(list);
	}

	@Unique
	private double wrapDistance(Entity entity, double x, double y, double z) {
		Level level = entity.level();
		if(level.isClientSide) return entity.distanceToSqr(x, y, z);

		WorldTransformer transformer = entity.level().getTransformer();
		return transformer.distanceToSqrWrapped(entity.position(), new Vec3(x, y, z));
	}
}
