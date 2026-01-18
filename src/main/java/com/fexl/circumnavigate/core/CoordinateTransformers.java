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

	public class CoordMethods {
		public final int domainLength = Math.abs(upperChunkBounds - lowerChunkBounds) * CoordinateConstants.CHUNK_WIDTH;

		private final int domainStart = lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH;
		private final int domainRadius = domainLength / 2;

		public double wrapToBounds(double coord) {
			//Short-circuit
			if(!isOverBounds(coord)) return coord;

			double wrappedCoord = (coord - domainStart) % domainLength;

			// If wrappedCoord is negative, adjust it by adding domainLength
			if (wrappedCoord < 0) {
				wrappedCoord += domainLength;
			}

			return domainStart + wrappedCoord;
		}

		public double unwrapFromBounds(double refCoord, double wrappedCoord) {
			double unwrappedCoord = refCoord + wrappedCoord - wrapToBounds(refCoord);

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refCoord - domainRadius) {
				unwrappedCoord += domainLength;
			}
			if (unwrappedCoord > refCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public int wrapToBounds(int coord) {
			return (int) wrapToBounds((double) coord);
		}

		public int unwrapFromBounds(int refCoord, int wrappedCoord) {
			return (int) unwrapFromBounds((double) refCoord, wrappedCoord);
		}

		public boolean isOverBounds(double coord) {
			return coord >= upperChunkBounds * CoordinateConstants.CHUNK_WIDTH || coord < lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH;
		}

		public boolean isOverBounds(int coord) {
			return isOverBounds((double) coord);
		}

		public double deltaFromBounds(double fromCoord, double toCoord) {
			double toCoordUnwrapped = unwrapFromBounds(fromCoord, toCoord);

			return toCoordUnwrapped - fromCoord;
		}

		public double sqrDistToBounds(double dist) {
			if(dist > upperChunkBounds * CoordinateConstants.CHUNK_WIDTH) {
				dist -= Coord.domainLength;
			}
			else if (dist < lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH) {
				dist += Coord.domainLength;
			}

			return dist * dist;
		}

		public int sqrDistToBounds(int dist) {
			return (int) sqrDistToBounds((double) dist);
		}
	}

	public class ChunkMethods {
		public final int domainLength = Coord.domainLength/16;

		public int wrapToBounds(int chunkCoord) {
			return Coord.wrapToBounds(chunkCoord*16)/16;
		}

		public int unwrapFromBounds(int refChunkCoord, int wrappedChunkCoord) {
			return Coord.unwrapFromBounds(refChunkCoord*16, wrappedChunkCoord*16)/16;
		}

		public boolean isOverBounds(int chunkCoord) {
			return Coord.isOverBounds(chunkCoord*16);
		}

		public int sqrDistToBounds(int chunkDist) {
			return Coord.sqrDistToBounds(chunkDist*16)/(16*16);
		}
	}
}
