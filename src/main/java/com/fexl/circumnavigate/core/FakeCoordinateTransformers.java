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
		public double wrap(double coord) {
			return coord;
		}

		public int wrap(int coord) {
			return coord;
		}

		@Override
		public double unwrap(double refCoord, double wrappedCoord) {
			return wrappedCoord;
		}

		public int unwrap(int refCoord, int wrappedCoord) {
			return wrappedCoord;
		}

		@Override
		public boolean isOver(double coord) {
			return false;
		}

		public boolean isOver(int coord) {
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
