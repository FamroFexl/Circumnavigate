/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.core;

import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for determining distances both directly, from point to point, and wrapped across the graph bounds to opposite sides.
 */
public class Distance {
	private int coord1;
	private int coord2;
	private int domainLength;
	private int directDistance;
	private int wrappedDistance;

	private int lowerChunkBounds;

	private int upperChunkBounds;

	private CoordinateTransformers transformer;

	public Distance(int coord1, int coord2, int lowerChunkBounds, int upperChunkBounds, CoordinateTransformers transformer) {
		this.coord1 = coord1;
		this.coord2 = coord2;

		this.lowerChunkBounds = lowerChunkBounds;
		this.upperChunkBounds = upperChunkBounds;

		this.transformer = transformer;

		this.domainLength = (upperChunkBounds * LevelChunkSection.SECTION_WIDTH) - (lowerChunkBounds * LevelChunkSection.SECTION_WIDTH);
		this.directDistance = Math.abs(coord2 - coord1);
		this.wrappedDistance = this.domainLength - this.directDistance;
	}

	public int wrappedDistance() {
		return this.wrappedDistance;
	}

	public int directDistance() {
		return this.directDistance;
	}

	/**
	 * Calculates what is the shortest distance between two points,
	 * the direct distance, or the wrapped distance
	 *
	 */
	public int minimumDistance() {
		return Math.min(this.directDistance, this.wrappedDistance);
	}

	/**
	 * Determines if the shortest distance between two points is direct,
	 * or wrapped across the world border.
	 */
	public boolean isMinDistWrapped() {
		if(this.minimumDistance() == this.wrappedDistance) { return true; }
		return false;
	}

	/**
	 * Calculates all wrapped coordinates between two coordinates
	 *
	 * @return Return all the coordinates within two wrapped coordinates
	 */
	public List<Integer> sumWrappedCoords(boolean isChunkCoords) {
		List<Integer> coords = new ArrayList<>();
		int increment = 1;

		int current = coord2;

		//Iterating from different directions
		if(coord1 < coord2)
			increment = -1;
			current = coord1;

		int wrapped;
		while(true) {
			if(isChunkCoords)
				wrapped = transformer.wrapChunkToLimit(current);
			else
				wrapped = transformer.wrapCoordToLimit(current);

			coords.add(wrapped);
			if(wrapped == coord2) {
				break;
			}
			current += increment;
		}
		return coords;
	}

	/**
	 * Calculates all wrapped coordinates between two intersecting Distances
	 *
	 * @param other Is used for secondary coordinates, such as z in (x,z)
	 * @return Return all the coordinates within two wrapped coordinates
	 */
	public List<Vec2> sumWrappedCoords2D(Distance other, boolean isChunkCoords) {
		List<Vec2> coords = new ArrayList<>();
		for(int coord1 : this.sumWrappedCoords(isChunkCoords)) {
			for(int coord2 : other.sumWrappedCoords(isChunkCoords)) {
				coords.add(new Vec2(coord1, coord2));
			}
		}

		return coords;
	}

	/**
	 * Calculates all direct coordinates between two coordinates
	 *
	 * @return Return all the coordinates within two regular coordinates
	 */
	public List<Integer> sumDirectCoords() {
		List<Integer> total = new ArrayList<>();
		int increment = 1;

		if(coord1 > coord2)
			increment = -1;

		for (int i = coord1; i <= coord2; i += increment) {
			total.add(i);
		}

		return total;
	}

	/**
	 * Calculates all coordinates between two intersecting Distances
	 *
	 * @param other Is used for secondary coordinates, such as z in (x,z)
	 * @return Return all the coordinates within two regular coordinates
	 */
	public List<Vec2> sumDirectCoords2D(Distance other) {
		List<Vec2> coords = new ArrayList<>();

		for(int coord1 : this.sumDirectCoords()) {
			for(int coord2 : other.sumDirectCoords()) {
				coords.add(new Vec2(coord1, coord2));
			}
		}

		return coords;
	}
}
