/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.interfaceInjects;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.injected.DimensionTransformerInjector;
import net.minecraft.world.level.PathNavigationRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PathNavigationRegion.class)
public class PathNavigationRegionInjectorMixin implements DimensionTransformerInjector {
	@Unique
	private DimensionTransformer transformer = null;

	@Override
	public DimensionTransformer getTransformer() {
		return transformer;
	}

	@Override
	public void setTransformer(DimensionTransformer transformer) {
		this.transformer = transformer;
	}
}
