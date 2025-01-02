/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.injected;

import com.fexl.circumnavigate.core.DimensionTransformer;

public interface DimensionTransformerInjector {
	default DimensionTransformer getTransformer() {
		return null;
	}

	default void setTransformer(DimensionTransformer transformer) {

	}
}
