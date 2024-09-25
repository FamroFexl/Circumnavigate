/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	/**
	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo ci) {
		if(level.isClientSide()) return;

		Entity thiz = (Entity)(Object)this;

		if(thiz instanceof Player) {
			return;
		}

		WorldTransformer transformer = level.getTransformer();

		if(transformer.xTransformer.isCoordOverLimit(thiz.getX()) || transformer.zTransformer.isCoordOverLimit(thiz.getZ())) {
			thiz.setPos(transformer.xTransformer.wrapCoordToLimit(thiz.getX()), thiz.getY(), transformer.zTransformer.wrapCoordToLimit(thiz.getZ()));
		}
	}**/

	@Shadow Level level;
	@Shadow Vec3 position;
	@Shadow BlockPos blockPosition;
	@Shadow BlockState feetBlockState;

	@Shadow ChunkPos chunkPosition;

	@Shadow EntityInLevelCallback levelCallback;

	@Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
	public void setPosRaw(double x, double y, double z, CallbackInfo ci) {
		/**
		ci.cancel();
		x = this.level.getTransformer().xTransformer.wrapCoordToLimit(x);
		z = this.level.getTransformer().zTransformer.wrapCoordToLimit(z);

		if (this.position.x != x || this.position.y != y || this.position.z != z) {
			this.position = new Vec3(x, y, z);

			int i = Mth.floor(x);
			int j = Mth.floor(y);
			int k = Mth.floor(z);
			if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
				this.blockPosition = new BlockPos(i, j, k);
				this.feetBlockState = null;
				if (SectionPos.blockToSectionCoord(i) != this.chunkPosition.x || SectionPos.blockToSectionCoord(k) != this.chunkPosition.z) {
					this.chunkPosition = new ChunkPos(this.blockPosition);
				}
			}

			this.levelCallback.onMove();
		}**/
	}
}
