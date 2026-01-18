/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.core;

public class CoordinateTransformers {
	public final int lowerChunkBounds;
	public final int upperChunkBounds;

	public final int lowerBlockBounds;
	public final int upperBlockBounds;

	public CoordMethods Coord;
	public ChunkMethods Chunk;

	public CoordinateTransformers(int lowerChunkBounds, int upperChunkBounds) {
		this.lowerChunkBounds = lowerChunkBounds;
		this.upperChunkBounds = upperChunkBounds;

		this.lowerBlockBounds = lowerChunkBounds * CoordinateConstants.CHUNK_WIDTH;
		this.upperBlockBounds = upperChunkBounds * CoordinateConstants.CHUNK_WIDTH;
		
		Coord = new CoordMethods();
		Chunk = new ChunkMethods();
	}

	public class CoordMethods {
		public final int domainLength = Math.abs(upperBlockBounds - lowerBlockBounds);

		private final int domainStart = lowerBlockBounds;
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

		public int wrapToBounds(int coord) {
			//Short-circuit
			if(!isOverBounds(coord)) return coord;

			int wrappedCoord = (coord - domainStart) % domainLength;

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
			else if (unwrappedCoord > refCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public int unwrapFromBounds(int refCoord, int wrappedCoord) {
			int unwrappedCoord = refCoord + wrappedCoord - wrapToBounds(refCoord);

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refCoord - domainRadius) {
				unwrappedCoord += domainLength;
			}
			else if (unwrappedCoord > refCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public boolean isOverBounds(double coord) {
			return coord >= upperBlockBounds || coord < lowerBlockBounds;
		}

		public boolean isOverBounds(int coord) {
			return coord >= upperBlockBounds || coord < lowerBlockBounds;
		}

		public double deltaFromBounds(double fromCoord, double toCoord) {
			double toCoordUnwrapped = unwrapFromBounds(fromCoord, toCoord);

			return toCoordUnwrapped - fromCoord;
		}

		public double sqrDistToBounds(double dist) {
			if(dist > upperBlockBounds) {
				dist -= domainLength;
			}
			else if (dist < lowerBlockBounds) {
				dist += domainLength;
			}

			return dist * dist;
		}

		public int sqrDistToBounds(int dist) {
			if(dist > upperBlockBounds) {
				dist -= domainLength;
			}
			else if (dist < lowerBlockBounds) {
				dist += domainLength;
			}

			return dist * dist;
		}
	}

	public class ChunkMethods {
		public final int domainLength = Math.abs(upperChunkBounds - lowerChunkBounds);

		private final int domainStart = lowerChunkBounds;
		private final int domainRadius = domainLength / 2;

		public int wrapToBounds(int chunkCoord) {
			//Short-circuit
			if(!isOverBounds(chunkCoord)) return chunkCoord;

			int wrappedCoord = (chunkCoord - domainStart) % domainLength;

			// If wrappedCoord is negative, adjust it by adding domainLength
			if (wrappedCoord < 0) {
				wrappedCoord += domainLength;
			}

			return domainStart + wrappedCoord;
		}

		public int unwrapFromBounds(int refChunkCoord, int wrappedChunkCoord) {
			int unwrappedCoord = refChunkCoord + wrappedChunkCoord - wrapToBounds(refChunkCoord);

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refChunkCoord - domainRadius) {
				unwrappedCoord += domainLength;
			}
			else if (unwrappedCoord > refChunkCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public boolean isOverBounds(int chunkCoord) {
			return chunkCoord >= upperChunkBounds || chunkCoord < lowerChunkBounds;
		}

		public int sqrDistToBounds(int chunkDist) {
			if(chunkDist > upperChunkBounds) {
				chunkDist -= domainLength;
			}
			else if (chunkDist < lowerChunkBounds) {
				chunkDist += domainLength;
			}

			return chunkDist * chunkDist;
		}
	}
}
