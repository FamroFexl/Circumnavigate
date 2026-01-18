/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.core;

public class FakeCoordinateTransformers extends CoordinateTransformers {
	public FakeCoordinateTransformers() {
		super(-CoordinateConstants.DISABLING_CHUNK_POS, CoordinateConstants.DISABLING_CHUNK_POS);

		this.Coord = new CoordMethods();
		this.Chunk = new ChunkMethods();
	}

	public class CoordMethods extends CoordinateTransformers.CoordMethods {
		public final int domainLength = CoordinateConstants.DISABLING_CHUNK_POS*2 * 16;

		@Override
		public double wrapToBounds(double coord) {
			return coord;
		}

		public int wrapToBounds(int coord) {
			return coord;
		}

		@Override
		public double unwrapFromBounds(double refCoord, double wrappedCoord) {
			return wrappedCoord;
		}

		public int unwrapFromBounds(int refCoord, int wrappedCoord) {
			return wrappedCoord;
		}

		@Override
		public boolean isOverBounds(double coord) {
			return false;
		}

		public boolean isOverBounds(int coord) {
			return false;
		}

		public double deltaFromBounds(double fromCoord, double toCoord) {
			return toCoord - fromCoord;
		}

		public double sqrDistToBounds(double dist) {
			return dist * dist;
		}

		public int sqrDistToBounds(int dist) {
			return dist * dist;
		}
	}
}
