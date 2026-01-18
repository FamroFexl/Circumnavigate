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
			return (int) wrapToBounds((double)coord);
		}

		@Override
		public double unwrapFromBounds(double refCoord, double wrappedCoord) {
			return wrappedCoord;
		}

		public int unwrapFromBounds(int refCoord, int wrappedCoord) {
			return (int) unwrapFromBounds((double) refCoord, wrappedCoord);
		}

		@Override
		public boolean isOverBounds(double coord) {
			return false;
		}

		public boolean isOverBounds(int coord) {
			return isOverBounds((double) coord);
		}

		public double deltaFromBounds(double fromCoord, double toCoord) {
			double toCoordUnwrapped = unwrapFromBounds(fromCoord, toCoord);

			return toCoordUnwrapped - fromCoord;
		}

		public double sqrDistToBounds(double dist) {
			return dist * dist;
		}

		public int sqrDistToBounds(int dist) {
			return (int) sqrDistToBounds((double) dist);
		}
	}
}
