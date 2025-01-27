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

		public double wrap(double coord) {
			//Short-circuit
			if(!isOver(coord)) return coord;

			double wrappedCoord = (coord - domainStart) % domainLength;

			// If wrappedCoord is negative, adjust it by adding domainLength
			if (wrappedCoord < 0) {
				wrappedCoord += domainLength;
			}

			return domainStart + wrappedCoord;
		}

		public int wrap(int coord) {
			//Short-circuit
			if(!isOver(coord)) return coord;

			int wrappedCoord = (coord - domainStart) % domainLength;

			// If wrappedCoord is negative, adjust it by adding domainLength
			if (wrappedCoord < 0) {
				wrappedCoord += domainLength;
			}

			return domainStart + wrappedCoord;
		}

		public double unwrap(double refCoord, double wrappedCoord) {
			double unwrappedCoord = refCoord + wrappedCoord - wrap(refCoord);

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refCoord - domainRadius) {
				unwrappedCoord += domainLength;
			}
			else if (unwrappedCoord > refCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public int unwrap(int refCoord, int wrappedCoord) {
			int unwrappedCoord = refCoord + wrappedCoord - wrap(refCoord);

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refCoord - domainRadius) {
				unwrappedCoord += domainLength;
			}
			else if (unwrappedCoord > refCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public boolean isOver(double coord) {
			return coord >= upperBlockBounds || coord < lowerBlockBounds;
		}

		public boolean isOver(int coord) {
			return coord >= upperBlockBounds || coord < lowerBlockBounds;
		}

		public double deltaFromBounds(double fromCoord, double toCoord) {
			double toCoordUnwrapped = unwrap(fromCoord, toCoord);

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

		public int wrap(int chunkCoord) {
			//Short-circuit
			if(!isOver(chunkCoord)) return chunkCoord;

			int wrappedCoord = (chunkCoord - domainStart) % domainLength;

			// If wrappedCoord is negative, adjust it by adding domainLength
			if (wrappedCoord < 0) {
				wrappedCoord += domainLength;
			}

			return domainStart + wrappedCoord;
		}

		public int unwrap(int refChunkCoord, int wrappedChunkCoord) {
			int unwrappedCoord = refChunkCoord + wrappedChunkCoord - wrap(refChunkCoord);

			// Adjust to ensure the unwrapped coordinate is correct
			if (unwrappedCoord < refChunkCoord - domainRadius) {
				unwrappedCoord += domainLength;
			}
			else if (unwrappedCoord > refChunkCoord + domainRadius) {
				unwrappedCoord -= domainLength;
			}

			return unwrappedCoord;
		}

		public boolean isOver(int chunkCoord) {
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
