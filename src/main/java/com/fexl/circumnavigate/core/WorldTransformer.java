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

	private final CoordinateTransformers xTransformer;
	private final CoordinateTransformers zTransformer;

	private final boolean isClientSide;

	private final int chunkWidth = LevelChunkSection.SECTION_WIDTH;

	public static final WorldTransformer INVALID = new WorldTransformer(WrappingSettings.invalidPos, true);

	//Accessor constants for various standard object operations
	public final CoordMethods Coord;
	public final ChunkMethods Chunk;
	public final SectionMethods Section;
	public final Vector3DMethods Vector3D;
	public final BlockMethods Block;
	public final AABBMethods AABB;

	public WorldTransformer(int x1, int z1, int x2, int z2, int xShift, int zShift, boolean isClientSide) {
		int invalidPos = WrappingSettings.invalidPos;

		this.isClientSide = isClientSide;

		//Not a wrapped world. Don't wrap.
		if(Math.abs(x1) == invalidPos || Math.abs(z1) == invalidPos || Math.abs(x2) == invalidPos || Math.abs(z2) == invalidPos) {
			//Set all values to impossibilities so no wrapping can take place
			this.xChunkBoundMin = this.zChunkBoundMin = -invalidPos;
			this.xChunkBoundMax = this.zChunkBoundMax = invalidPos;
			xShift = zShift = 0;

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

		this.xWidth = xTransformer.Chunk.domainLength;
		this.zWidth = zTransformer.Chunk.domainLength;

		this.xBlockWidth = this.xWidth*chunkWidth - 1;
		this.zBlockWidth = this.zWidth*chunkWidth - 1;

		this.centerX = (this.xChunkBoundMax + this.xChunkBoundMin) / 2;
		this.centerZ = (this.zChunkBoundMax + this.zChunkBoundMin) / 2;

		this.Coord = new CoordMethods();
		this.Chunk = new ChunkMethods();
		this.Section = new SectionMethods();
		this.Vector3D = new Vector3DMethods();
		this.Block = new BlockMethods();
		this.AABB = new AABBMethods();
	}

	/**
	 * For bounds without chunk shifting.
	 */
	public WorldTransformer(int x1, int z1, int x2, int z2, boolean isClientSide) {
		this(x1, z1, x2, z2, 0, 0, isClientSide);
	}

	/**
	 * For bounds centered at (0,0).
	 */
	public WorldTransformer(int xChunkBound, int zChunkBound, boolean isClientSide) {
		this(-xChunkBound, -zChunkBound, xChunkBound, zChunkBound, isClientSide);
	}

	/**
	 * For equivalently bounds at (0,0).
	 */
	public WorldTransformer(int chunkBound, boolean isClientSide) {
		this(chunkBound, chunkBound, isClientSide);
	}

	public WorldTransformer(ChunkPos min, ChunkPos max, int xShift, int zShift, boolean isClientSide) {
		this(min.x, min.z, max.x, max.z, xShift, zShift, isClientSide);
	}

	public WorldTransformer(ChunkPos min, ChunkPos max, boolean isClientSide) {
		this(min, max, 0, 0, isClientSide);
	}

	public WorldTransformer onlyServerSide() {
		if(!isClientSide) return this;
		return INVALID;
	}

	public WorldTransformer onlyClientSide() {
		if(isClientSide) return this;
		return INVALID;
	}

	/**
	 * Standard operations for Generic Coordinates
	 */
	public final class CoordMethods {
		public final CoordinateTransformers.CoordMethods X = xTransformer.Coord;
		public final CoordinateTransformers.CoordMethods Z = zTransformer.Coord;

		private double sqrDistToBounds(double x, double y, double z) {
			double returnX = Coord.X.sqrDistToBounds(x);
			double returnZ = Coord.Z.sqrDistToBounds(z);

			return returnX + y * y + returnZ;
		}

		public double sqrDistToBounds(double xFrom, double yFrom, double zFrom, double xTo, double yTo, double zTo) {
			double d = xTo - xFrom;
			double e = yTo - yFrom;
			double f = zTo - zFrom;

			return sqrDistToBounds(d, e, f);
		}
	}

	/**
	 * Standard operations for {@code ChunkPos } and Chunk Coordinates
	 */
	public final class ChunkMethods extends BasicPositionOperations<ChunkPos> {
		public final CoordinateTransformers.ChunkMethods X = xTransformer.Chunk;
		public final CoordinateTransformers.ChunkMethods Z = zTransformer.Chunk;

		@Override
		public ChunkPos wrapToBounds(ChunkPos chunkPos) {
			return new ChunkPos(X.wrapToBounds(chunkPos.x), Z.wrapToBounds(chunkPos.z));
		}

		@Override
		public ChunkPos unwrapFromBounds(ChunkPos refChunkPos, ChunkPos wrappedChunkPos) {
			return new ChunkPos(X.unwrapFromBounds(refChunkPos.x, wrappedChunkPos.x), Z.unwrapFromBounds(refChunkPos.z, wrappedChunkPos.z));
		}

		@Override
		public boolean isOverBounds(ChunkPos chunkPos) {
			return X.isOverBounds(chunkPos.x) || Z.isOverBounds(chunkPos.z);
		}

		private int sqrDistToBounds(int x, int z) {
			int returnX = Chunk.X.sqrDistToBounds(x);
			int returnZ = Chunk.Z.sqrDistToBounds(z);

			return returnX + returnZ;
		}

		public int sqrDistToBounds(Long chunkPos1, Long chunkPos2) {
			return sqrDistToBounds(ChunkPos.getX(chunkPos1), ChunkPos.getZ(chunkPos1), ChunkPos.getX(chunkPos2), ChunkPos.getZ(chunkPos2));
		}

		public int sqrDistToBounds(ChunkPos chunkPos1, ChunkPos chunkPos2) {
			return sqrDistToBounds(chunkPos1.x, chunkPos1.z, chunkPos2.x, chunkPos2.z);
		}

		public int sqrDistToBounds(int xFrom, int zFrom, int xTo, int zTo) {
			int returnX = xTo - xFrom;
			int returnZ = zTo - zFrom;

			return Chunk.sqrDistToBounds(returnX, returnZ);
		}
	}

	/**
	 * Standard operations for {@code SectionPos}
	 */
	public final class SectionMethods extends BasicPositionOperations<SectionPos> {
		@Override
		public SectionPos wrapToBounds(SectionPos sectionPos) {
			return SectionPos.of(Chunk.X.wrapToBounds(sectionPos.x()), sectionPos.y(), Chunk.Z.wrapToBounds(sectionPos.z()));
		}

		@Override
		public SectionPos unwrapFromBounds(SectionPos refCoord, SectionPos wrappedCoord) {
			return SectionPos.of(Chunk.X.unwrapFromBounds(refCoord.x(), wrappedCoord.x()), wrappedCoord.y(), Chunk.Z.unwrapFromBounds(refCoord.z(), wrappedCoord.z()));
		}

		@Override
		public boolean isOverBounds(SectionPos sectionPos) {
			return Chunk.X.isOverBounds(sectionPos.x()) || Chunk.Z.isOverBounds(sectionPos.z());
		}
	}

	/**
	 * Standard operations for {@code Vec3}
	 */
	public final class Vector3DMethods extends BasicPositionOperations<Vec3> {
		@Override
		public Vec3 wrapToBounds(Vec3 vec3) {
			return new Vec3(Coord.X.wrapToBounds(vec3.x), vec3.y, Coord.Z.wrapToBounds(vec3.z));
		}

		@Override
		public Vec3 unwrapFromBounds(Vec3 refVec3, Vec3 wrappedVec3) {
			return new Vec3(Coord.X.unwrapFromBounds(refVec3.x, wrappedVec3.x), wrappedVec3.y, Coord.Z.unwrapFromBounds(refVec3.z, wrappedVec3.z));
		}

		@Override
		public boolean isOverBounds(Vec3 vec3) {
			return Coord.X.isOverBounds(vec3.x) || Coord.Z.isOverBounds(vec3.z);
		}

		public double sqrDistToBounds(Vec3 from, Vec3 to) {
			double d = to.x - from.x;
			double e = to.y - from.y;
			double f = to.z - from.z;

			return Coord.sqrDistToBounds(d, e, f);
		}
	}

	/**
	 * Standard operations for {@code BlockPos}
	 */
	public final class BlockMethods extends BasicPositionOperations<BlockPos> {
		@Override
		public BlockPos wrapToBounds(BlockPos blockPos) {
			return new BlockPos(Coord.X.wrapToBounds(blockPos.getX()), blockPos.getY(), Coord.Z.wrapToBounds(blockPos.getZ()));
		}

		public long wrapToBounds(long blockPos) {
			return wrapToBounds(new BlockPos(BlockPos.getX(blockPos), BlockPos.getY(blockPos), BlockPos.getZ(blockPos))).asLong();
		}

		@Override
		public BlockPos unwrapFromBounds(BlockPos refBlockPos, BlockPos wrappedBlockPos) {
			return new BlockPos(Coord.X.unwrapFromBounds(refBlockPos.getX(), wrappedBlockPos.getX()), wrappedBlockPos.getY(), Coord.Z.unwrapFromBounds(refBlockPos.getZ(), wrappedBlockPos.getZ()));
		}

		@Override
		public boolean isOverBounds(BlockPos blockPos) {
			return Coord.X.isOverBounds(blockPos.getX()) || Coord.Z.isOverBounds(blockPos.getZ());
		}
	}

	/**
	 * Standard operations for {@code AABB}
	 */
	public final class AABBMethods extends BasicPositionOperations<AABB> {
		@Override
		public AABB unwrapFromBounds(AABB refAABB, AABB wrappedAABB) {
			double minX = Coord.X.unwrapFromBounds(refAABB.minX , wrappedAABB.minX);
			double maxX = Coord.X.unwrapFromBounds(refAABB.maxX , wrappedAABB.maxX);
			double minZ = Coord.Z.unwrapFromBounds(refAABB.minZ , wrappedAABB.minZ);
			double maxZ = Coord.Z.unwrapFromBounds(refAABB.maxZ , wrappedAABB.maxZ);

			return new AABB(minX, wrappedAABB.minY, minZ, maxX, wrappedAABB.maxY, maxZ);
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
			if(!(Coord.X.isOverBounds(minX) || Coord.X.isOverBounds(maxX) || Coord.X.isOverBounds(minZ) || Coord.X.isOverBounds(maxZ))) return List.of(original);

			double minXWrapped = Coord.X.wrapToBounds(minX);
			double maxXWrapped = Coord.X.wrapToBounds(maxX);
			double minZWrapped = Coord.Z.wrapToBounds(minZ);
			double maxZWrapped = Coord.Z.wrapToBounds(maxZ);

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

		@Override
		public boolean isOverBounds(AABB aabb) {
			return (Coord.X.isOverBounds(aabb.minX) || Coord.X.isOverBounds(aabb.maxX) || Coord.Z.isOverBounds(aabb.minZ) || Coord.Z.isOverBounds(aabb.maxZ));
		}
	}

	public double distanceToSqrWrappedCoord(AABB aabb, Vec3 vec) {
		double d = Math.max(Math.max(aabb.minX - vec.x, vec.x - aabb.maxX), 0.0);
		double e = Math.max(Math.max(aabb.minY - vec.y, vec.y - aabb.maxY), 0.0);
		double f = Math.max(Math.max(aabb.minZ - vec.z, vec.z - aabb.maxZ), 0.0);

		return Coord.sqrDistToBounds(d, e, f);
	}

	/**
	 * Adjusts a viewDistance to be within a 3 chunk boundary of a wrapped axis' radius.
	 */
	public int limitViewDistance(int viewDistance) {
		int min = Math.min(this.xWidth / 2, this.zWidth / 2) - 3;
		return Math.min(viewDistance, min);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[xMin: " + this.xChunkBoundMin + ", xMax: " + this.xChunkBoundMax + ", zMin: " + this.zChunkBoundMin + ", zMax: " + this.zChunkBoundMax + ", xShift: " + this.xShift + ", zShift: " + this.zShift + "]";
	}

	public boolean isWrapped() {
		int invalidPos = WrappingSettings.invalidPos;
		return !(xChunkBoundMin == -invalidPos || xChunkBoundMax == invalidPos || zChunkBoundMin == -invalidPos || zChunkBoundMax == invalidPos);
	}
}
