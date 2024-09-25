/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.injected.LevelTransformerInjector;
import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Injects a transformer and accessor methods into Level instances. This means transformers are stored on a per-level basis.
 */
@Mixin(Level.class)
public class LevelTransformerInjectorMixin implements LevelTransformerInjector {
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
