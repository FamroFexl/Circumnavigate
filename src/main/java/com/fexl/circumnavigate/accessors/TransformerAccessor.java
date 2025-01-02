/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.accessors;

import com.fexl.circumnavigate.core.DimensionTransformer;

public interface TransformerAccessor {
	DimensionTransformer getTransformer();
	void setTransformer(DimensionTransformer transformer);
}
