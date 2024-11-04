/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.interfaceInjects;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.injected.LevelTransformerInjector;
import net.minecraft.world.level.PathNavigationRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PathNavigationRegion.class)
public class PathNavigationRegionInjectorMixin implements LevelTransformerInjector {
	@Unique
	private WorldTransformer transformer = null;

	@Override
	public WorldTransformer getTransformer() {
		return transformer;
	}

	@Override
	public void setTransformer(WorldTransformer transformer) {
		this.transformer = transformer;
	}
}
