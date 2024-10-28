/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle.collisions;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelMixin<T extends Entity> {
	@Shadow protected abstract LevelEntityGetter<Entity> getEntities();
	@Shadow public abstract ProfilerFiller getProfiler();

	Level thiz = (Level) (Object) this;

	/**
	 * Returns all entities within the bounds of a wrapped bounding box.
	 */
	@Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At("HEAD"), cancellable = true)
	public void getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate, List<? super T> output, int maxResults, CallbackInfo ci) {
		if(thiz.isClientSide) return;
		ci.cancel();
		WorldTransformer transformer = thiz.getTransformer();

		this.getProfiler().incrementCounter("getEntities");
		List<AABB> boxes = transformer.splitAcrossBounds(bounds);
		for(AABB box : boxes) {
			this.getEntities().get(entityTypeTest, box, entity -> {
				if (predicate.test(entity)) {
					output.add(entity);
					if (output.size() >= maxResults) {
						return AbortableIterationConsumer.Continuation.ABORT;
					}
				}

				if (entity instanceof EnderDragon enderDragon) {
					for (EnderDragonPart enderDragonPart : enderDragon.getSubEntities()) {
						T entity2 = entityTypeTest.tryCast(enderDragonPart);
						if (entity2 != null && predicate.test(entity2)) {
							output.add(entity2);
							if (output.size() >= maxResults) {
								return AbortableIterationConsumer.Continuation.ABORT;
							}
						}
					}
				}

				return AbortableIterationConsumer.Continuation.CONTINUE;
			});
		}
	}


	@ModifyVariable(method = "getBlockState", at = @At("HEAD"), argsOnly = true, index = 1)
	public BlockPos getBlockState(BlockPos blockPos) {
		if(thiz.isClientSide) return blockPos;
		return thiz.getTransformer().translateBlockToBounds(blockPos);
	}

	@ModifyVariable(method = "getFluidState", at = @At("HEAD"), argsOnly = true, index = 1)
	public BlockPos getFluidState(BlockPos blockPos) {
		if(thiz.isClientSide) return blockPos;
		return thiz.getTransformer().translateBlockToBounds(blockPos);
	}
}
