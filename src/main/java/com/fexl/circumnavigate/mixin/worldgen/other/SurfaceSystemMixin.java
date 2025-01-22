/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen.other;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SurfaceSystem.class)
public class SurfaceSystemMixin {
	@Shadow @Final private static BlockState WHITE_TERRACOTTA;
	@Shadow @Final private static BlockState ORANGE_TERRACOTTA;
	@Shadow @Final private static BlockState TERRACOTTA;
	@Shadow @Final private static BlockState YELLOW_TERRACOTTA;
	@Shadow @Final private static BlockState BROWN_TERRACOTTA;
	@Shadow @Final private static BlockState RED_TERRACOTTA;
	@Shadow @Final private static BlockState LIGHT_GRAY_TERRACOTTA;
	@Shadow @Final private static BlockState PACKED_ICE;
	@Shadow @Final private static BlockState SNOW_BLOCK;
	@Shadow @Final private BlockState defaultBlock;
	@Shadow @Final private int seaLevel;
	@Shadow @Final private BlockState[] clayBands;
	@Shadow @Final private NormalNoise clayBandsOffsetNoise;
	@Shadow @Final private NormalNoise badlandsPillarNoise;
	@Shadow @Final private NormalNoise badlandsPillarRoofNoise;
	@Shadow @Final private NormalNoise badlandsSurfaceNoise;
	@Shadow @Final private NormalNoise icebergPillarNoise;
	@Shadow @Final private NormalNoise icebergPillarRoofNoise;
	@Shadow @Final private NormalNoise icebergSurfaceNoise;
	@Shadow @Final private PositionalRandomFactory noiseRandom;
	@Shadow @Final private NormalNoise surfaceNoise;
	@Shadow @Final private NormalNoise surfaceSecondaryNoise;
	
	private void erodedBadlandsExtension(BlockColumn blockColumn, int x, int z, int height, LevelHeightAccessor level) {
		double d = 0.2;

		((NoiseScaling) (Object) this.badlandsPillarNoise).setMul(0.2);
		double e = Math.min(
			Math.abs(this.badlandsSurfaceNoise.getValue((double)x, 0.0, (double)z) * 8.25),
			this.badlandsPillarNoise.getValue((double)x * 0.2, 0.0, (double)z * 0.2) * 15.0
		);
		if (!(e <= 0.0)) {
			double f = 0.75;
			double g = 1.5;
			((NoiseScaling) (Object) this.badlandsPillarRoofNoise).setMul(0.75);
			//double h = Math.abs(this.badlandsPillarRoofNoise.getValue((double)x * 0.75, 0.0, (double)z * 0.75) * 1.5);
			double h = Math.abs(this.badlandsPillarRoofNoise.getValue((double)x, 0.0, (double)z) * 1.5);
			double i = 64.0 + Math.min(e * e * 2.5, Math.ceil(h * 50.0) + 24.0);
			int j = Mth.floor(i);
			if (height <= j) {
				for (int k = j; k >= level.getMinBuildHeight(); k--) {
					BlockState blockState = blockColumn.getBlock(k);
					if (blockState.is(this.defaultBlock.getBlock())) {
						break;
					}

					if (blockState.is(Blocks.WATER)) {
						return;
					}
				}

				for (int k = j; k >= level.getMinBuildHeight() && blockColumn.getBlock(k).isAir(); k--) {
					blockColumn.setBlock(k, this.defaultBlock);
				}
			}
		}
	}

	private void frozenOceanExtension(int minSurfaceLevel, Biome biome, BlockColumn blockColumn, BlockPos.MutableBlockPos topWaterPos, int x, int z, int height) {
		double d = 1.28;

		((NoiseScaling) (Object) this.icebergPillarNoise).setMul(1.28);
		//double e = Math.min(Math.abs(this.icebergSurfaceNoise.getValue((double)x, 0.0, (double)z) * 8.25), this.icebergPillarNoise.getValue((double)x * 1.28, 0.0, (double)z * 1.28) * 15.0);
		double e = Math.min(Math.abs(this.icebergSurfaceNoise.getValue((double)x, 0.0, (double)z) * 8.25), this.icebergPillarNoise.getValue((double)x, 0.0, (double)z) * 15.0);
		if (!(e <= 1.8)) {
			double f = 1.17;
			double g = 1.5;
			((NoiseScaling) (Object) this.icebergPillarRoofNoise).setMul(1.17);
			//double h = Math.abs(this.icebergPillarRoofNoise.getValue((double)x * 1.17, 0.0, (double)z * 1.17) * 1.5);
			double h = Math.abs(this.icebergPillarRoofNoise.getValue((double)x, 0.0, (double)z) * 1.5);
			double i = Math.min(e * e * 1.2, Math.ceil(h * 40.0) + 14.0);
			if (biome.shouldMeltFrozenOceanIcebergSlightly(topWaterPos.set(x, 63, z))) {
				i -= 2.0;
			}

			double j;
			if (i > 2.0) {
				j = (double)this.seaLevel - i - 7.0;
				i += (double)this.seaLevel;
			} else {
				i = 0.0;
				j = 0.0;
			}

			double k = i;
			RandomSource randomSource = this.noiseRandom.at(x, 0, z);
			int l = 2 + randomSource.nextInt(4);
			int m = this.seaLevel + 18 + randomSource.nextInt(10);
			int n = 0;

			for (int o = Math.max(height, (int)i + 1); o >= minSurfaceLevel; o--) {
				if (blockColumn.getBlock(o).isAir() && o < (int)k && randomSource.nextDouble() > 0.01
					|| blockColumn.getBlock(o).is(Blocks.WATER) && o > (int)j && o < this.seaLevel && j != 0.0 && randomSource.nextDouble() > 0.15) {
					if (n <= l && o > m) {
						blockColumn.setBlock(o, SNOW_BLOCK);
						n++;
					} else {
						blockColumn.setBlock(o, PACKED_ICE);
					}
				}
			}
		}
	}
	
}
