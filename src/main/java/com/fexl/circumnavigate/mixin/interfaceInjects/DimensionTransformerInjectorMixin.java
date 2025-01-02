/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.interfaceInjects;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.injected.DimensionTransformerInjector;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects a transformer and accessor methods into Level instances. This means transformers are stored on a per-level basis.
 */
@Mixin(Level.class)
public class DimensionTransformerInjectorMixin implements DimensionTransformerInjector {
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
