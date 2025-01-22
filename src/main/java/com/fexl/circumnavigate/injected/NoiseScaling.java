/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.injected;

public interface NoiseScaling {
	default void setMul(double mul) { setXMul(mul); setZMul(mul);}
	void setXMul(double xMul);
	void setZMul(double zMul);
	void setXAdd(double xAdd);
	void setZAdd(double zAdd);
}