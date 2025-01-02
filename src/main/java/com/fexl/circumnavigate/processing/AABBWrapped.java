/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBWrapped extends AABB {
	final DimensionTransformer transformer;
	public AABBWrapped(AABB aabb, DimensionTransformer transformer) {
		super(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		this.transformer = transformer;
	}

	@Override
	public double distanceToSqr(Vec3 vec) {
		return transformer.distanceToSqrWrappedCoord(this, vec);
	}
}
