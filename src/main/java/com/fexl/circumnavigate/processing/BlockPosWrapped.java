package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class BlockPosWrapped extends BlockPos {
    final DimensionTransformer transformer;
    public BlockPosWrapped(BlockPos blockPos, DimensionTransformer transformer) {
        super(transformer.Coord.X.wrap(blockPos.getX()), blockPos.getY(), transformer.Coord.Z.wrap(blockPos.getZ()));
        this.transformer = transformer;
    }

	private BlockPosWrapped(int x, int y, int z, DimensionTransformer transformer) {
		this(new BlockPos(x, y, z), transformer);
	}

    public static long offset(long pos, int dx, int dy, int dz, Level level) {
        return level.getTransformer().Block.wrap(asLong(getX(pos) + dx, getY(pos) + dy, getZ(pos) + dz));
    }

    @Override
    public @NotNull BlockPosWrapped offset(int dx, int dy, int dz) {
        return dx == 0 && dy == 0 && dz == 0 ? this : new BlockPosWrapped(this.getX() + dx, this.getY() + dy, this.getZ() + dz, this.transformer);
    }

    @Override
    public @NotNull BlockPosWrapped multiply(int scalar) {
        if (scalar == 1) {
            return this;
        } else {
            return scalar == 0 ? (BlockPosWrapped) ZERO : new BlockPosWrapped(this.getX() * scalar, this.getY() * scalar, this.getZ() * scalar, this.transformer);
        }
    }

    @Override
    public @NotNull BlockPosWrapped relative(Direction direction) {
        return new BlockPosWrapped(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ(), this.transformer);
    }

    @Override
    public @NotNull BlockPosWrapped relative(Direction direction, int distance) {
        return distance == 0
                ? this
                : new BlockPosWrapped(
    this.getX() + direction.getStepX() * distance, this.getY() + direction.getStepY() * distance, this.getZ() + direction.getStepZ() * distance, this.transformer);
    }

    @Override
    public @NotNull BlockPosWrapped relative(Direction.Axis axis, int amount) {
        if (amount == 0) {
            return this;
        } else {
            int i = axis == Direction.Axis.X ? amount : 0;
            int j = axis == Direction.Axis.Y ? amount : 0;
            int k = axis == Direction.Axis.Z ? amount : 0;
            return new BlockPosWrapped(this.getX() + i, this.getY() + j, this.getZ() + k, this.transformer);
        }
    }

    @Override
    public @NotNull BlockPosWrapped cross(Vec3i vector) {
        return new BlockPosWrapped(
                this.getY() * vector.getZ() - this.getZ() * vector.getY(),
                this.getZ() * vector.getX() - this.getX() * vector.getZ(),
                this.getX() * vector.getY() - this.getY() * vector.getX(),
	            this.transformer
        );
    }

    @Deprecated
    public static Stream<BlockPos> squareOutSouthEast(BlockPos pos, DimensionTransformer transformer) {
		BlockPosWrapped wrappedBlockPos = new BlockPosWrapped(pos, transformer);
        return Stream.of(wrappedBlockPos, wrappedBlockPos.south(), wrappedBlockPos.east(), wrappedBlockPos.south().east());
    }

    public static class MutableBlockPos extends BlockPosWrapped {
        public MutableBlockPos(int x, int y, int z, DimensionTransformer transformer) {
            super(x, y, z, transformer);
        }

        public MutableBlockPos(double x, double y, double z, DimensionTransformer transformer) {
            this(Mth.floor(x), Mth.floor(y), Mth.floor(z), transformer);
        }

        public BlockPosWrapped.@NotNull MutableBlockPos setX(int x) {
            x = transformer.Coord.X.wrap(x);
            super.setX(x);
            return this;
        }

        public BlockPosWrapped.@NotNull MutableBlockPos setZ(int z) {
            z = transformer.Coord.Z.wrap(z);
            super.setZ(z);
            return this;
        }
    }


}
