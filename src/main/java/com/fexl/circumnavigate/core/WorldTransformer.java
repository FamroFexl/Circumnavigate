/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.core;

import com.fexl.circumnavigate.options.WrappingSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores wrapping constants and provides world-wrapping operations.
 */
public class WorldTransformer {

	public final int xChunkBoundMin;
	public final int xChunkBoundMax;

	public final int zChunkBoundMin;
	public final int zChunkBoundMax;

	public final int xCoordBoundMin;
	public final int xCoordBoundMax;

	public final int zCoordBoundMin;
	public final int zCoordBoundMax;

	public final int xBlockBoundMin;
	public final int xBlockBoundMax;

	public final int zBlockBoundMin;
	public final int zBlockBoundMax;

	//Only one can be set (non-zero) at a time.
	//TODO: Implement chunk border shifting.
	public final int xShift;
	public final int zShift;

	public final int xWidth;

	public final int zWidth;

	public final int xBlockWidth;

	public final int zBlockWidth;

	public final int centerX;

	public final int centerZ;

	public final CoordinateTransformers xTransformer;
	public final CoordinateTransformers zTransformer;

	private final int chunkWidth = LevelChunkSection.SECTION_WIDTH;

	public static final WorldTransformer INVALID = new WorldTransformer(WrappingSettings.invalidPos);

	public WorldTransformer(int x1, int z1, int x2, int z2, int xShift, int zShift) {
		int invalidPos = WrappingSettings.invalidPos;

		//Not a wrapped world. Don't wrap.
		if(Math.abs(x1) == invalidPos || Math.abs(z1) == invalidPos || Math.abs(x2) == invalidPos || Math.abs(z2) == invalidPos) {
			//Set all values to impossibilities so no wrapping can take place
			this.xChunkBoundMin = this.zChunkBoundMin = -invalidPos;
			this.xChunkBoundMax = this.zChunkBoundMax = invalidPos;
			xShift = zShift = 0;

			//Not required, but eliminates unnecessary wrapping calculations for the client.
			this.xTransformer = new FakeCoordinateTransformers();
			this.zTransformer = new FakeCoordinateTransformers();
		}
		//Wrapped world. Wrap it.
		else {
			this.xChunkBoundMin = Math.min(x1, x2);
			this.zChunkBoundMin = Math.min(z1, z2);
			this.xChunkBoundMax = Math.max(x1, x2);
			this.zChunkBoundMax = Math.max(z1, z2);


			this.xTransformer = new CoordinateTransformers(this.xChunkBoundMin, this.xChunkBoundMax);
			this.zTransformer = new CoordinateTransformers(this.zChunkBoundMin, this.zChunkBoundMax);
		}

		this.xCoordBoundMin = this.xChunkBoundMin * chunkWidth;
		this.xCoordBoundMax = this.xChunkBoundMax * chunkWidth;
		this.zCoordBoundMin = this.zChunkBoundMin * chunkWidth;
		this.zCoordBoundMax = this.zChunkBoundMax * chunkWidth;

		this.xBlockBoundMin = this.xChunkBoundMin * chunkWidth;
		this.xBlockBoundMax = this.xChunkBoundMax * chunkWidth - 1;
		this.zBlockBoundMin = this.zChunkBoundMin * chunkWidth;
		this.zBlockBoundMax = this.zChunkBoundMax * chunkWidth - 1;

		this.xShift = xShift;
		this.zShift = zShift;

		this.xWidth = Math.abs(this.xChunkBoundMin) + Math.abs(this.xChunkBoundMax);
		this.zWidth = Math.abs(this.zChunkBoundMin) + Math.abs(this.zChunkBoundMax);

		this.xBlockWidth = this.xWidth*chunkWidth - 1;
		this.zBlockWidth = this.zWidth*chunkWidth - 1;

		this.centerX = (this.xChunkBoundMax + this.xChunkBoundMin) / 2;
		this.centerZ = (this.zChunkBoundMax + this.zChunkBoundMin) / 2;
	}

	/**
	 * For bounds without chunk shifting.
	 */
	public WorldTransformer(int x1, int z1, int x2, int z2) {
		this(x1, z1, x2, z2, 0, 0);
	}

	/**
	 * For bounds centered at (0,0).
	 */
	public WorldTransformer(int xChunkBound, int zChunkBound) {
		this(-xChunkBound, -zChunkBound, xChunkBound, zChunkBound);
	}

	/**
	 * For equivalently bounds at (0,0).
	 */
	public WorldTransformer(int chunkBound) {
		this(chunkBound, chunkBound);
	}

	public WorldTransformer(ChunkPos min, ChunkPos max, int xShift, int zShift) {
		this(min.x, min.z, max.x, max.z, xShift, zShift);
	}

	public WorldTransformer(ChunkPos min, ChunkPos max) {
		this(min, max, 0, 0);
	}

	public Vec3 translateVecToBounds(Vec3 vec3) {
		double returnX = xTransformer.wrapCoordToLimit(vec3.x);
		double returnZ = zTransformer.wrapCoordToLimit(vec3.z);

		return new Vec3(returnX, vec3.y, returnZ);
	}

	public Vec3 translateVecFromBounds(Vec3 relVec3, Vec3 wrappedVec3) {
		double returnX = xTransformer.unwrapCoordFromLimit(relVec3.x, wrappedVec3.x);
		double returnZ = zTransformer.unwrapCoordFromLimit(relVec3.z, wrappedVec3.z);

		return new Vec3(returnX, wrappedVec3.y, returnZ);
	}

	public BlockPos translateBlockToBounds(BlockPos blockPos) {
		int returnX = xTransformer.wrapCoordToLimit(blockPos.getX());
		int returnZ = zTransformer.wrapCoordToLimit(blockPos.getZ());

		return new BlockPos(returnX, blockPos.getY(), returnZ);
	}

	public long translateBlockToBounds(long blockPos) {
		int returnX = xTransformer.wrapCoordToLimit(BlockPos.getX(blockPos));
		int returnZ = zTransformer.wrapCoordToLimit(BlockPos.getZ(blockPos));

		return new BlockPos(returnX, BlockPos.getY(blockPos), returnZ).asLong();
	}

	public BlockPos translateBlockFromBounds(BlockPos relBlockPos, BlockPos wrappedBlockPos) {
		int returnX = xTransformer.unwrapCoordFromLimit(relBlockPos.getX(), wrappedBlockPos.getX());
		int returnZ = zTransformer.unwrapCoordFromLimit(relBlockPos.getZ(), wrappedBlockPos.getZ());

		return new BlockPos(returnX, wrappedBlockPos.getY(), returnZ);
	}

	public ChunkPos translateChunkToBounds(ChunkPos chunkPos) {
		int returnX = xTransformer.wrapChunkToLimit(chunkPos.x);
		int returnZ = zTransformer.wrapChunkToLimit(chunkPos.z);

		return new ChunkPos(returnX, returnZ);
	}

	public SectionPos translateSectionToBounds(SectionPos sectionPos) {
		int returnX = xTransformer.wrapChunkToLimit(sectionPos.x());
		int returnZ = zTransformer.wrapChunkToLimit(sectionPos.z());

		return SectionPos.of(returnX, sectionPos.y(), returnZ);
	}

	public long translateChunkToBounds(long chunkPos) {
		return translateChunkToBounds(new ChunkPos(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos))).toLong();
	}

	public ChunkPos translateChunkFromBounds(ChunkPos relChunkPos, ChunkPos wrappedChunkPos) {
		int returnX = xTransformer.unwrapChunkFromLimit(relChunkPos.x, wrappedChunkPos.x);
		int returnZ = zTransformer.unwrapChunkFromLimit(relChunkPos.z, wrappedChunkPos.z);

		return new ChunkPos(returnX, returnZ);
	}

	public long translateChunkFromBounds(long relChunkPos, long wrappedChunkPos) {
		return translateChunkFromBounds(new ChunkPos(ChunkPos.getX(relChunkPos), ChunkPos.getZ(relChunkPos)), new ChunkPos(ChunkPos.getX(wrappedChunkPos), ChunkPos.getZ(wrappedChunkPos))).toLong();
	}

	public AABB translateAABBFromBounds(AABB relBox, AABB wrappedBox) {
		double minX = xTransformer.unwrapCoordFromLimit(relBox.minX , wrappedBox.minX);
		double maxX = xTransformer.unwrapCoordFromLimit(relBox.maxX , wrappedBox.maxX);
		double minZ = zTransformer.unwrapCoordFromLimit(relBox.minZ , wrappedBox.minZ);
		double maxZ = zTransformer.unwrapCoordFromLimit(relBox.maxZ , wrappedBox.maxZ);

		return new AABB(minX, wrappedBox.minY, minZ, maxX, wrappedBox.maxY, maxZ);
	}

	/**
	 * Splits an AABB into up to 4 separate AABB depending on bounds overlap.
	 */
	public List<AABB> splitAcrossBounds(AABB original) {
		double minX = original.minX;
		double maxX = original.maxX;
		double minZ = original.minZ;
		double maxZ = original.maxZ;

		//Guard clause
		if(!(xTransformer.isCoordOverLimit(minX) || xTransformer.isCoordOverLimit(maxX) || zTransformer.isCoordOverLimit(minZ) || zTransformer.isCoordOverLimit(maxZ))) return List.of(original);

		double minXWrapped = xTransformer.wrapCoordToLimit(minX);
		double maxXWrapped = xTransformer.wrapCoordToLimit(maxX);
		double minZWrapped = zTransformer.wrapCoordToLimit(minZ);
		double maxZWrapped = zTransformer.wrapCoordToLimit(maxZ);

		List<AABB> list = new ArrayList<>();

		double minY = original.minY;
		double maxY = original.maxY;

		if((minX != minXWrapped || maxX != maxXWrapped) && (minZ != minZWrapped || maxZ != maxZWrapped)) {
			list.add(new AABB(xCoordBoundMin,   minY,   minZWrapped,    maxXWrapped,    maxY,   zCoordBoundMax));
			list.add(new AABB(xCoordBoundMin,   minY,   zCoordBoundMin, maxXWrapped,    maxY,   maxZWrapped));
			list.add(new AABB(minXWrapped,      minY,   zCoordBoundMin, xCoordBoundMax, maxY,   maxZWrapped));
			list.add(new AABB(minXWrapped,      minY,   minZWrapped,    xCoordBoundMax, maxY,   zCoordBoundMax));
		}
		else if(minX != minXWrapped || maxX != maxXWrapped) {
			list.add(new AABB(xCoordBoundMin,   minY,   minZ,           maxXWrapped,    maxY,   maxZ));
			list.add(new AABB(minXWrapped,      minY,   minZ,           xCoordBoundMax, maxY,   maxZ));
		}
		else if(minZ != minZWrapped || maxZ != maxZWrapped) {
			list.add(new AABB(minX,             minY,   minZWrapped,    maxX,           maxY,   xCoordBoundMax));
			list.add(new AABB(minX,             minY,   xCoordBoundMin, maxX,           maxY,   maxZWrapped));
		}
		else {
			list.add(original);
		}

		return list;
	}

	public boolean isChunkOverBounds(ChunkPos chunkPos) {
		return xTransformer.isChunkOverLimit(chunkPos.x) || zTransformer.isChunkOverLimit(chunkPos.z);
	}

	public boolean isBlockOverBounds(BlockPos blockPos) {
		return xTransformer.isCoordOverLimit(blockPos.getX()) || zTransformer.isCoordOverLimit(blockPos.getZ());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[xMin: " + this.xChunkBoundMin + ", xMax: " + this.xChunkBoundMax + ", zMin: " + this.zChunkBoundMin + ", zMax: " + this.zChunkBoundMax + ", xShift: " + this.xShift + ", zShift: " + this.zShift + "]";
	}

	public boolean isWrapped() {
		int invalidPos = WrappingSettings.invalidPos;
		return !(xChunkBoundMin == -invalidPos || xChunkBoundMax == invalidPos || zChunkBoundMin == -invalidPos || zChunkBoundMax == invalidPos);
	}

	public int distanceToSqrWrapped(long chunkPos1, long chunkPos2) {
		return (int) distanceToSqrWrapped(ChunkPos.getX(chunkPos1), 0, ChunkPos.getZ(chunkPos1), ChunkPos.getX(chunkPos2), 0, ChunkPos.getZ(chunkPos2));
	}

	public int distanceToSqrWrapped(ChunkPos chunkPos1, ChunkPos chunkPos2) {
		return (int) distanceToSqrWrapped(chunkPos1.x, 0, chunkPos1.z, chunkPos2.x, 0, chunkPos2.z);
	}

	public double distanceToSqrWrapped(Vec3 from, Vec3 to) {
		double d = to.x - from.x;
		double e = to.y - from.y;
		double f = to.z - from.z;

		return wrapAndSqr(d, e, f);
	}

	public double distanceToSqrWrapped(double xFrom, double yFrom, double zFrom, double xTo, double yTo, double zTo) {
		double d = xTo - xFrom;
		double e = yTo - yFrom;
		double f = zTo - zFrom;

		return wrapAndSqr(d, e, f);
	}

	public double distanceToSqrWrapped(AABB aabb, Vec3 vec) {
		double d = Math.max(Math.max(aabb.minX - vec.x, vec.x - aabb.maxX), 0.0);
		double e = Math.max(Math.max(aabb.minY - vec.y, vec.y - aabb.maxY), 0.0);
		double f = Math.max(Math.max(aabb.minZ - vec.z, vec.z - aabb.maxZ), 0.0);

		return wrapAndSqr(d, e, f);
	}

	private double wrapAndSqr(double x, double y, double z) {
		int xWidthCoord = xWidth*chunkWidth;
		int zWidthCoord = zWidth*chunkWidth;

		// Adjust for wrapping on the x-axis
		if (x > xChunkBoundMax) {
			x -= xWidthCoord;
		} else if (x < xChunkBoundMin) {
			x += xWidthCoord;
		}

		// Adjust for wrapping on the z-axis
		if (z > zChunkBoundMax) {
			z -= zWidthCoord;
		} else if (z < zChunkBoundMin) {
			z += zWidthCoord;
		}
		return x * x + y * y + z * z;
	}

	/**
	 * Adjusts a viewDistance to be within a 2 chunk boundary of a wrapped axis' radius.
	 */
	public int limitViewDistance(int viewDistance) {
		int min = Math.min(this.xWidth / 2, this.zWidth / 2) - 2;
		return Math.min(viewDistance, min);
	}
}
