/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
