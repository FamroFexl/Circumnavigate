/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.core;

import com.fexl.circumnavigate.options.DimensionWrappingSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores wrapping constants and provides dimension-wrapping operations.
 */
public class DimensionTransformer {
	public final int xWidth;

	public final int zWidth;

	public final int centerX;

	public final int centerZ;

	private final CoordinateTransformers xTransformer;
	private final CoordinateTransformers zTransformer;

	private final boolean isClientSide;

	public static final DimensionTransformer DISABLED = new DimensionTransformer(new DimensionWrappingSettings(CoordinateConstants.DISABLING_CHUNK_POS), false);

	//Accessor constants for various standard object operations
	public final CoordMethods Coord;
	public final ChunkMethods Chunk;
	public final SectionMethods Section;
	public final Vector3DMethods Vector3D;
	public final BlockMethods Block;
	public final AABBMethods AABoundingBox;
	public final BoundingBoxMethods BoundingBox;

	public final DimensionWrappingSettings wrappingSettings;

	public DimensionTransformer(DimensionWrappingSettings wrappingSettings, boolean isClientSide) {
		this.wrappingSettings = wrappingSettings;
		this.isClientSide = isClientSide;


		if(this.wrappingSettings.xChunkBoundMin() == -CoordinateConstants.DISABLING_CHUNK_POS || this.wrappingSettings.xChunkBoundMax() == CoordinateConstants.DISABLING_CHUNK_POS)
			this.xTransformer = new FakeCoordinateTransformers();
		else
			this.xTransformer = new CoordinateTransformers(wrappingSettings.xChunkBoundMin(), wrappingSettings.xChunkBoundMax());

		if(this.wrappingSettings.zChunkBoundMin() == -CoordinateConstants.DISABLING_CHUNK_POS || this.wrappingSettings.zChunkBoundMax() == CoordinateConstants.DISABLING_CHUNK_POS)
			this.zTransformer = new FakeCoordinateTransformers();
		else
			this.zTransformer = new CoordinateTransformers(wrappingSettings.zChunkBoundMin(), wrappingSettings.zChunkBoundMax());

		this.xWidth = xTransformer.Chunk.domainLength;
		this.zWidth = zTransformer.Chunk.domainLength;

		this.centerX = (wrappingSettings.xChunkBoundMax() + wrappingSettings.xChunkBoundMin()) / 2;
		this.centerZ = (wrappingSettings.zChunkBoundMax() + wrappingSettings.zChunkBoundMin()) / 2;

		this.Coord = new CoordMethods();
		this.Chunk = new ChunkMethods();
		this.Section = new SectionMethods();
		this.Vector3D = new Vector3DMethods();
		this.Block = new BlockMethods();
		this.AABoundingBox = new AABBMethods();
		this.BoundingBox = new BoundingBoxMethods();
	}

	/** Server-side only transformer **/
	public DimensionTransformer SSO() {
		return isClientSide ? DISABLED : this;
	}

	/** Client-Side only transformer **/
	public DimensionTransformer CSO() {
		return isClientSide ? this : DISABLED;
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
		public ChunkPos wrap(ChunkPos chunkPos) {
			return new ChunkPos(X.wrap(chunkPos.x), Z.wrap(chunkPos.z));
		}

		public ChunkPos wrap(long chunkPos) {
			return wrap(new ChunkPos(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos)));
		}

		@Override
		public ChunkPos unwrap(ChunkPos refChunkPos, ChunkPos wrappedChunkPos) {
			return new ChunkPos(X.unwrap(refChunkPos.x, wrappedChunkPos.x), Z.unwrap(refChunkPos.z, wrappedChunkPos.z));
		}

		@Override
		public boolean isOver(ChunkPos chunkPos) {
			return X.isOver(chunkPos.x) || Z.isOver(chunkPos.z);
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
		public SectionPos wrap(SectionPos sectionPos) {
			return SectionPos.of(Chunk.X.wrap(sectionPos.x()), sectionPos.y(), Chunk.Z.wrap(sectionPos.z()));
		}

		@Override
		public SectionPos unwrap(SectionPos refCoord, SectionPos wrappedCoord) {
			return SectionPos.of(Chunk.X.unwrap(refCoord.x(), wrappedCoord.x()), wrappedCoord.y(), Chunk.Z.unwrap(refCoord.z(), wrappedCoord.z()));
		}

		@Override
		public boolean isOver(SectionPos sectionPos) {
			return Chunk.X.isOver(sectionPos.x()) || Chunk.Z.isOver(sectionPos.z());
		}
	}

	/**
	 * Standard operations for {@code Vec3}
	 */
	public final class Vector3DMethods extends BasicPositionOperations<Vec3> {
		@Override
		public Vec3 wrap(Vec3 vec3) {
			return new Vec3(Coord.X.wrap(vec3.x), vec3.y, Coord.Z.wrap(vec3.z));
		}

		@Override
		public Vec3 unwrap(Vec3 refVec3, Vec3 wrappedVec3) {
			return new Vec3(Coord.X.unwrap(refVec3.x, wrappedVec3.x), wrappedVec3.y, Coord.Z.unwrap(refVec3.z, wrappedVec3.z));
		}

		@Override
		public boolean isOver(Vec3 vec3) {
			return Coord.X.isOver(vec3.x) || Coord.Z.isOver(vec3.z);
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
		public BlockPos wrap(BlockPos blockPos) {
			return new BlockPos(Coord.X.wrap(blockPos.getX()), blockPos.getY(), Coord.Z.wrap(blockPos.getZ()));
		}

		public long wrap(long blockPos) {
			return wrap(new BlockPos(BlockPos.getX(blockPos), BlockPos.getY(blockPos), BlockPos.getZ(blockPos))).asLong();
		}

		@Override
		public BlockPos unwrap(BlockPos refBlockPos, BlockPos wrappedBlockPos) {
			return new BlockPos(Coord.X.unwrap(refBlockPos.getX(), wrappedBlockPos.getX()), wrappedBlockPos.getY(), Coord.Z.unwrap(refBlockPos.getZ(), wrappedBlockPos.getZ()));
		}

		@Override
		public boolean isOver(BlockPos blockPos) {
			return Coord.X.isOver(blockPos.getX()) || Coord.Z.isOver(blockPos.getZ());
		}

		public BlockPos deltaFromBounds(BlockPos refBlockPos, BlockPos wrappedBlockPos) {
			return new BlockPos(Coord.X.deltaFromBounds(refBlockPos.getX(), wrappedBlockPos.getX()), wrappedBlockPos.getY(), Coord.Z.deltaFromBounds(refBlockPos.getZ(), wrappedBlockPos.getZ()));
		}
	}

	/**
	 * Standard operations for {@code AABB}
	 */
	public final class AABBMethods extends BasicPositionOperations<AABB> {
		@Override
		public AABB unwrap(AABB refAABB, AABB wrappedAABB) {
			double minX = Coord.X.unwrap(refAABB.minX , wrappedAABB.minX);
			double maxX = Coord.X.unwrap(refAABB.maxX , wrappedAABB.maxX);
			double minZ = Coord.Z.unwrap(refAABB.minZ , wrappedAABB.minZ);
			double maxZ = Coord.Z.unwrap(refAABB.maxZ , wrappedAABB.maxZ);

			return new AABB(minX, wrappedAABB.minY, minZ, maxX, wrappedAABB.maxY, maxZ);
		}

		/**
		 * Splits an AABB into up to 4 separate AABB depending on bounds overlap.
		 */
		public List<AABB> splitAcrossBounds(AABB original) {
			int xCoordBoundMin = wrappingSettings.xChunkBoundMin() * CoordinateConstants.CHUNK_WIDTH;
			int zCoordBoundMin = wrappingSettings.zChunkBoundMin() * CoordinateConstants.CHUNK_WIDTH;
			int xCoordBoundMax = wrappingSettings.xChunkBoundMax() * CoordinateConstants.CHUNK_WIDTH;
			int zCoordBoundMax = wrappingSettings.zChunkBoundMax() * CoordinateConstants.CHUNK_WIDTH;

			double minX = original.minX;
			double maxX = original.maxX;
			double minZ = original.minZ;
			double maxZ = original.maxZ;

			//Guard clause
			if(!(Coord.X.isOver(minX) || Coord.X.isOver(maxX) || Coord.X.isOver(minZ) || Coord.X.isOver(maxZ))) return List.of(original);

			double minXWrapped = Coord.X.wrap(minX);
			double maxXWrapped = Coord.X.wrap(maxX);
			double minZWrapped = Coord.Z.wrap(minZ);
			double maxZWrapped = Coord.Z.wrap(maxZ);

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
		public boolean isOver(AABB aabb) {
			return (Coord.X.isOver(aabb.minX) || Coord.X.isOver(aabb.maxX) || Coord.Z.isOver(aabb.minZ) || Coord.Z.isOver(aabb.maxZ));
		}
	}

	public final class BoundingBoxMethods extends BasicPositionOperations<BoundingBox> {
		public BoundingBox unwrap(AABB refAABB, AABB originalAABB) {
			AABB out = AABoundingBox.unwrap(refAABB, originalAABB);

			return new BoundingBox((int) Math.floor(out.minX), (int) Math.floor(out.minY), (int) Math.floor(out.minZ), (int) Math.floor(out.maxX), (int) Math.floor(out.maxY),(int) Math.floor(out.maxZ));
		}

		@Override
		public BoundingBox unwrap(BoundingBox refBoundingBox, BoundingBox originalBoundingBox) {
			AABB refAABB = new AABB(refBoundingBox.minX(), refBoundingBox.minY(), refBoundingBox.minZ(), refBoundingBox.maxX(), refBoundingBox.maxY(), refBoundingBox.maxZ());
			AABB originalAABB = new AABB(originalBoundingBox.minX(), originalBoundingBox.minY(), originalBoundingBox.minZ(), originalBoundingBox.maxX(), originalBoundingBox.maxY(), originalBoundingBox.maxZ());

			return unwrap(refAABB, originalAABB);
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
		return Math.min(viewDistance, getMaxViewDistance());
	}

	public int getMaxViewDistance() {
		return Math.min(this.xWidth / 2, this.zWidth / 2) - CoordinateConstants.MIN_VIEW_DISTANCE_BUFFER;
	}

	@Override
	public String toString() {
		String shifting = (wrappingSettings.shiftAmount() != 0) ? (", shiftAxis: " + wrappingSettings.shiftAxis() + ", shiftAmount: " + wrappingSettings.shiftAmount()) : "";
		return this.getClass().getSimpleName() + "[xMin: " + wrappingSettings.xChunkBoundMin() + ", xMax: " + wrappingSettings.xChunkBoundMax() + ", zMin: " + wrappingSettings.zChunkBoundMin() + ", zMax: " + wrappingSettings.zChunkBoundMax() + shifting + "]";
	}

	public boolean isWrapped() {
		return !wrappingSettings.equals(DISABLED.wrappingSettings);
	}
}
