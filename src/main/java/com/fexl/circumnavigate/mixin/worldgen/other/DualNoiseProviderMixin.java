/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DualNoiseProvider.class)
public class DualNoiseProviderMixin {
	@Shadow @Final private float slowScale;
	@Shadow @Final private NormalNoise slowNoise;

	public double getSlowNoiseValue(BlockPos pos) {
		((NoiseScaling) (Object) this.slowNoise).setMul(this.slowScale);
		//return this.slowNoise.getValue((double)((float)pos.getX() * this.slowScale), (double)((float)pos.getY() * this.slowScale), (double)((float)pos.getZ() * this.slowScale));
		return this.slowNoise.getValue((double)((float)pos.getX()), (double)((float)pos.getY() * this.slowScale), (double)((float)pos.getZ()));
	}

}
