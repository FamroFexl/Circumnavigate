/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entity.collisions;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.google.common.collect.AbstractIterator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
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

	@Mutable @Shadow @Final private AABB box;
	@Mutable @Shadow @Final private Cursor3D cursor;
	@Mutable @Shadow @Final private VoxelShape entityShape;

	/**
	 * Keep box, shape, and cursor in the same frame.
	 * */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void wrap3DCursor(CollisionGetter collisionGetter, Entity entity, AABB box, boolean onlySuffocatingBlocks, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider, CallbackInfo ci) {
		this.serverLevel = null;
		if (collisionGetter instanceof ServerLevel serverLevel) this.serverLevel = serverLevel;

		// Only apply wrapped collision logic on the server.
		if (entity == null || this.serverLevel == null) {
			return;
		}

		// Unwrap against the entity frame.
		AABB adjustedBox = entity.level().getTransformer().AABoundingBox.unwrap(entity.getBoundingBox(), box);
		if (!isSameBox(adjustedBox, box)) {
			this.box = adjustedBox;
			this.entityShape = Shapes.create(adjustedBox);
			this.cursor = createCursor(adjustedBox);
		}
	}

	/**
	 * Wraps thee BlockCollisions.getChunk() method.
	 */
	@WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockCollisions;getChunk(II)Lnet/minecraft/world/level/BlockGetter;"))
	public BlockGetter getChunk(BlockCollisions<?> instance, int x, int z, Operation<BlockGetter> original) {
		if (this.serverLevel == null) return original.call(instance, x, z);
		DimensionTransformer transformer = this.serverLevel.getTransformer();
		return original.call(instance, transformer.Coord.X.wrap(x), transformer.Coord.Z.wrap(z));
	}

	/**
	 * Wraps the MutableBlockPos.set() method.
	 */
	@WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;"))
	public BlockPos.MutableBlockPos setPos(BlockPos.MutableBlockPos instance, int x, int y, int z, Operation<BlockPos.MutableBlockPos> original) {
		if (this.serverLevel == null) return original.call(instance, x, y, z);
		DimensionTransformer transformer = this.serverLevel.getTransformer();
		return original.call(instance, transformer.Coord.X.wrap(x), y, transformer.Coord.Z.wrap(z));
	}

	@Unique
	private static boolean isSameBox(AABB left, AABB right) {
		return left.minX == right.minX && left.minY == right.minY && left.minZ == right.minZ && left.maxX == right.maxX && left.maxY == right.maxY && left.maxZ == right.maxZ;
	}

	/**
	 * Vanilla cursor bounds. See {@link BlockCollisions} constructor.
	 * */
	@Unique
	private static Cursor3D createCursor(AABB box) {
		int minX = Mth.floor(box.minX - 1.0E-7) - 1;
		int maxX = Mth.floor(box.maxX + 1.0E-7) + 1;
		int minY = Mth.floor(box.minY - 1.0E-7) - 1;
		int maxY = Mth.floor(box.maxY + 1.0E-7) + 1;
		int minZ = Mth.floor(box.minZ - 1.0E-7) - 1;
		int maxZ = Mth.floor(box.maxZ + 1.0E-7) + 1;
		return new Cursor3D(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
