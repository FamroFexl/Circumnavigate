/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.core;

public class CoordinateTransformers {
	public final int lowerChunkBounds;
	public final int upperChunkBounds;

	public CoordMethods Coord;
	public ChunkMethods Chunk;

	public CoordinateTransformers(int lowerChunkBounds, int upperChunkBounds) {
		this.lowerChunkBounds = lowerChunkBounds;
		this.upperChunkBounds = upperChunkBounds;
		
		Coord = new CoordMethods();
		Chunk = new ChunkMethods();
	}

	public class CoordMethods extends BasicPositionOperations<Double> {
		public final int domainLength = Math.abs(upperChunkBounds - lowerChunkBounds) * CoordinateConstants.CHUNK_WIDTH;

		@Override
		public Double wrapToBounds(Double coord) {
			//Short-circuit
			if(!isOverBounds(coord)) return coord;

			double domainStart = lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH;
			double wrappedCoord = (coord - domainStart) % domainLength;

			// If wrappedCoord is negative, adjust it by adding domainLength
			if (wrappedCoord < 0) {
				wrappedCoord += domainLength;
			}

			return domainStart + wrappedCoord;
		}

		public Integer wrapToBounds(Integer coord) {
			return wrapToBounds(coord.doubleValue()).intValue();
		}

		@Override
		public Double unwrapFromBounds(Double refCoord, Double wrappedCoord) {
			double wrappedRefCoord = wrapToBounds(refCoord);

			double diff = wrappedCoord - wrappedRefCoord;

			double unwrappedCoord = refCoord + diff;

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refCoord - (double) domainLength / 2) {
				unwrappedCoord += domainLength;
			}
			if (unwrappedCoord > refCoord + (double) domainLength / 2) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public Integer unwrapFromBounds(Integer refCoord, Integer wrappedCoord) {
			return unwrapFromBounds(refCoord.doubleValue(), wrappedCoord.doubleValue()).intValue();
		}

		@Override
		public boolean isOverBounds(Double coord) {
			return coord >= upperChunkBounds * CoordinateConstants.CHUNK_WIDTH || coord < lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH;
		}

		public boolean isOverBounds(Integer coord) {
			return isOverBounds(coord.doubleValue());
		}

		public Double deltaFromBounds(Double fromCoord, Double toCoord) {
			double toCoordUnwrapped = unwrapFromBounds(fromCoord, toCoord);

			return toCoordUnwrapped - fromCoord;
		}

		public Integer deltaFromBounds(Integer fromCoord, Integer toCoord) {
			return deltaFromBounds(fromCoord.doubleValue(), toCoord.doubleValue()).intValue();
		}

		public Double sqrDistToBounds(Double dist) {
			if(dist > upperChunkBounds * CoordinateConstants.CHUNK_WIDTH) {
				dist -= Coord.domainLength;
			}
			else if (dist < lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH) {
				dist += Coord.domainLength;
			}

			return dist * dist;
		}

		public Integer sqrDistToBounds(Integer dist) {
			return sqrDistToBounds(dist.doubleValue()).intValue();
		}
	}

	public class ChunkMethods extends BasicPositionOperations<Integer> {
		public final int domainLength = Coord.domainLength/16;

		@Override
		public Integer wrapToBounds(Integer chunkCoord) {
			return Coord.wrapToBounds(chunkCoord*16)/16;
		}

		@Override
		public Integer unwrapFromBounds(Integer refChunkCoord, Integer wrappedChunkCoord) {
			return Coord.unwrapFromBounds(refChunkCoord*16, wrappedChunkCoord*16)/16;
		}

		@Override
		public boolean isOverBounds(Integer chunkCoord) {
			return Coord.isOverBounds(chunkCoord*16);
		}

		public int sqrDistToBounds(int chunkDist) {
			return Coord.sqrDistToBounds(chunkDist*16)/16;
		}
	}
}
