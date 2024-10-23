/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.Cursor3D;

/**
 * Wraps outputs by manipulating them via a transformer.
 */
public class Cursor3DWrapped extends Cursor3D {
	final WorldTransformer transformer;

	public Cursor3DWrapped(int originX, int originY, int originZ, int endX, int endY, int endZ, WorldTransformer transformer) {
		super(originX, originY, originZ, endX, endY, endZ);
		this.transformer = transformer;
	}

	@Override
	public int nextX() {
		return transformer.xTransformer.wrapCoordToLimit(super.nextX());
	}

	@Override
	public int nextZ() {
		return transformer.zTransformer.wrapCoordToLimit(super.nextZ());
	}
}
