/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entity.collisions;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.google.common.collect.AbstractIterator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin<T> extends AbstractIterator<T> {
	@Unique
	ServerLevel serverLevel;

	/**
	 * Provides the serverLevel, if it exists.
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void wrap3DCursor(CollisionGetter collisionGetter, Entity entity, AABB box, boolean onlySuffocatingBlocks, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider, CallbackInfo ci) {
		serverLevel = null;
		if(collisionGetter instanceof ServerLevel level) serverLevel = level;
	}

	/**
	 * Wraps thee BlockCollisions.getChunk() method.
	 */
	@WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockCollisions;getChunk(II)Lnet/minecraft/world/level/BlockGetter;"))
	public BlockGetter getChunk(BlockCollisions<?> instance, int x, int z, Operation<BlockGetter> original) {
		if(serverLevel == null) return original.call(instance, x, z);
		DimensionTransformer transformer = serverLevel.getTransformer();
		return original.call(instance, (int) transformer.Coord.X.wrapToBounds(x), (int) transformer.Coord.Z.wrapToBounds(z));
	}

	/**
	 * Wraps the MutableBlockPos.set() method.
	 */
	@WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;"))
	public BlockPos.MutableBlockPos setPos(BlockPos.MutableBlockPos instance, int x, int y, int z, Operation<BlockPos.MutableBlockPos> original) {
		if(serverLevel == null) return original.call(instance, x, y, z);
		DimensionTransformer transformer = serverLevel.getTransformer();
		return original.call(instance, (int) transformer.Coord.X.wrapToBounds(x), y, (int) transformer.Coord.Z.wrapToBounds(z));
	}
}
