/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.core;

public class FakeCoordinateTransformers extends CoordinateTransformers {
	public FakeCoordinateTransformers() {
		super(-WorldTransformer.invalidPos, WorldTransformer.invalidPos);

		this.Coord = new CoordMethods();
		this.Chunk = new ChunkMethods();
	}

	public class CoordMethods extends CoordinateTransformers.CoordMethods {
		public final int domainLength = WorldTransformer.invalidPos*2;

		@Override
		public Double wrapToBounds(Double coord) {
			return coord;
		}

		public Integer wrapToBounds(Integer coord) {
			return wrapToBounds(coord.doubleValue()).intValue();
		}

		@Override
		public Double unwrapFromBounds(Double refCoord, Double wrappedCoord) {
			return wrappedCoord;
		}

		public Integer unwrapFromBounds(Integer refCoord, Integer wrappedCoord) {
			return unwrapFromBounds(refCoord.doubleValue(), wrappedCoord.doubleValue()).intValue();
		}

		@Override
		public boolean isOverBounds(Double coord) {
			return false;
		}

		public boolean isOverBounds(Integer coord) {
			return isOverBounds(coord.doubleValue());
		}

		public Double deltaFromBounds(Double fromCoord, Double toCoord) {
			double toCoordUnwrapped = unwrapFromBounds(fromCoord, toCoord);

			return toCoordUnwrapped - fromCoord;
		}

		public Double sqrDistToBounds(Double dist) {
			return dist * dist;
		}

		public Integer sqrDistToBounds(Integer dist) {
			return sqrDistToBounds(dist.doubleValue()).intValue();
		}
	}
}
