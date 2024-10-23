/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle.collisions;

import com.fexl.circumnavigate.processing.Cursor3DWrapped;
import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
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
	@Mutable @Final @Shadow private Cursor3D cursor;

	/**
	 * Wraps block collisions.
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(CollisionGetter collisionGetter, Entity entity, AABB box, boolean onlySuffocatingBlocks, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider, CallbackInfo ci) {
		if(collisionGetter instanceof Level level && level.isClientSide) return;

		int i = Mth.floor(box.minX - 1.0E-7) - 1;
		int j = Mth.floor(box.maxX + 1.0E-7) + 1;
		int k = Mth.floor(box.minY - 1.0E-7) - 1;
		int l = Mth.floor(box.maxY + 1.0E-7) + 1;
		int m = Mth.floor(box.minZ - 1.0E-7) - 1;
		int n = Mth.floor(box.maxZ + 1.0E-7) + 1;

		this.cursor = new Cursor3DWrapped(i, k, m, j, l, n, collisionGetter.getTransformer());


	}
}
