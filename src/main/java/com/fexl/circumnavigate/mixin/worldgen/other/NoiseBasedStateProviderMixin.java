/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseBasedStateProvider.class)
public class NoiseBasedStateProviderMixin {
	@Shadow @Final protected NormalNoise noise;

	public double getNoiseValue(BlockPos pos, double delta) {
		((NoiseScaling) (Object) this.noise).setMul(delta);
		//return this.noise.getValue((double)pos.getX() * delta, (double)pos.getY() * delta, (double)pos.getZ() * delta);
		return this.noise.getValue((double)pos.getX(), (double)pos.getY() * delta, (double)pos.getZ());
	}
}
