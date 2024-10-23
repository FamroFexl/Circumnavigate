/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow private Level level;

	/**
	 * Modifies the inputted X position of the entity to be within the wrapping bounds
	 */
	@ModifyVariable(method = "setPosRaw", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public double setPosRawX(double x) {
		if (level.isClientSide()) return x;

		Entity thiz = (Entity)(Object)this;

		if(thiz instanceof Player) {
			return x;
		}

		return this.level.getTransformer().xTransformer.wrapCoordToLimit(x);
	}

	/**
	 * Modifies the inputted Z position of the entity to be within the wrapping bounds
	 */
	@ModifyVariable(method = "setPosRaw", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	public double setPosRawZ(double z) {
		if (level.isClientSide()) return z;

		Entity thiz = (Entity)(Object)this;

		if(thiz instanceof Player) {
			return z;
		}

		return this.level.getTransformer().zTransformer.wrapCoordToLimit(z);
	}

	/**
	 * Checks if an entity is colliding with a block. Modified to support wrapped worlds.
	 */
	@Redirect(method = "isColliding", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/Shapes;joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z"))
	public boolean create(VoxelShape shape1, VoxelShape shape2, BooleanOp resultOperator) {
		if (level.isClientSide()) return Shapes.joinIsNotEmpty(shape1, shape2, resultOperator);

		WorldTransformer transformer = this.level.getTransformer();
		VoxelShape result = Shapes.create(transformer.translateAABBFromBounds(shape1.bounds(), shape2.bounds()));

		return Shapes.joinIsNotEmpty(shape1, result, resultOperator);
	}
}
