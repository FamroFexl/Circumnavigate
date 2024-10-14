/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.core;

import com.fexl.circumnavigate.options.WrappingSettings;

/**
 * Extends the wrapping bounds to invalid chunk positions, so they don't affect unwrapped worlds.
 */
public class FakeCoordinateTransformers extends CoordinateTransformers {
	public FakeCoordinateTransformers() {
		super(WrappingSettings.invalidPos);
	}

	@Override
	public double wrapCoordToLimit(double coord) {
		return coord;
	}

	@Override
	public int wrapCoordToLimit(int coord) {
		return (int) wrapCoordToLimit((double) coord);
	}

	@Override
	public int wrapChunkToLimit(int chunkCoord) {
		return chunkCoord;
	}

	@Override
	public int unwrapCoordFromLimit(int refCoord, int wrappedCoord) {
		return wrappedCoord;
	}

	@Override
	public int unwrapChunkFromLimit(int refChunkCoord, int wrappedChunkCoord) {
		return wrappedChunkCoord;
	}


}
