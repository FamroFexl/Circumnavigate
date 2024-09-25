/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.injected;

import com.fexl.circumnavigate.core.WorldTransformer;

public interface LevelTransformerInjector {
	default WorldTransformer getTransformer() {
		return null;
	}

	default void setTransformer(WorldTransformer transformer) {

	}
}
