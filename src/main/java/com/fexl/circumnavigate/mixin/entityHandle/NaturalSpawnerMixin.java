/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	/**
	 * Prevents entities from spawning past the borders and causing issues with {@link net.minecraft.world.entity.Entity#setPosRaw(double, double, double)}.
	 */
	@Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
	private static void spawnOverBoundsCancel(SpawnPlacements.Type placeType, LevelReader level, BlockPos pos, @Nullable EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
		if(level instanceof ServerLevelAccessor) {
			ServerLevelAccessor serverLevel = (ServerLevelAccessor) level;
			WorldTransformer transformer = serverLevel.getLevel().getTransformer();

			if(transformer.xTransformer.isCoordOverLimit(pos.getX()) || transformer.zTransformer.isCoordOverLimit(pos.getZ())) {
				cir.setReturnValue(false);
			}
		}
	}
}
