package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class BlockPosWrapped extends BlockPos {
    final WorldTransformer transformer;
    public BlockPosWrapped(BlockPos blockPos, WorldTransformer transformer) {
        super(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.transformer = transformer;
    }

    public static long offset(long pos, int dx, int dy, int dz, Level level) {
        return level.getTransformer().translateBlockToBounds(asLong(getX(pos) + dx, getY(pos) + dy, getZ(pos) + dz));
    }

    @Override
    public @NotNull BlockPos offset(int dx, int dy, int dz) {
        return dx == 0 && dy == 0 && dz == 0 ? this : blockToBounds(new BlockPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz));
    }

    @Override
    public @NotNull BlockPos multiply(int scalar) {
        if (scalar == 1) {
            return this;
        } else {
            return scalar == 0 ? ZERO : blockToBounds(new BlockPos(this.getX() * scalar, this.getY() * scalar, this.getZ() * scalar));
        }
    }

    @Override
    public @NotNull BlockPos relative(Direction direction) {
        return blockToBounds(new BlockPos(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ()));
    }

    @Override
    public @NotNull BlockPos relative(Direction direction, int distance) {
        return distance == 0
                ? this
                : blockToBounds(new BlockPos(
    this.getX() + direction.getStepX() * distance, this.getY() + direction.getStepY() * distance, this.getZ() + direction.getStepZ() * distance));
    }

    @Override
    public @NotNull BlockPos relative(Direction.Axis axis, int amount) {
        if (amount == 0) {
            return this;
        } else {
            int i = axis == Direction.Axis.X ? amount : 0;
            int j = axis == Direction.Axis.Y ? amount : 0;
            int k = axis == Direction.Axis.Z ? amount : 0;
            return blockToBounds(new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k));
        }
    }

    @Override
    public @NotNull BlockPos cross(Vec3i vector) {
        return blockToBounds(new BlockPos(
                this.getY() * vector.getZ() - this.getZ() * vector.getY(),
                this.getZ() * vector.getX() - this.getX() * vector.getZ(),
                this.getX() * vector.getY() - this.getY() * vector.getX()
        ));
    }

    @Deprecated
    public static Stream<BlockPos> squareOutSouthEast(BlockPos pos, WorldTransformer transformer) {
        return Stream.of(pos, blockToBounds(pos.south(), transformer), blockToBounds(pos.east(), transformer), blockToBounds(pos.south().east(), transformer));
    }

    public static class MutableBlockPosWrapped extends MutableBlockPos {
        final WorldTransformer transformer;

        public MutableBlockPosWrapped(int x, int y, int z, WorldTransformer transformer) {
            super(x, y, z);
            this.transformer = transformer;
        }

        public MutableBlockPosWrapped(double x, double y, double z, WorldTransformer transformer) {
            this(Mth.floor(x), Mth.floor(y), Mth.floor(z), transformer);
        }

        public BlockPosWrapped.@NotNull MutableBlockPos setX(int x) {
            x = transformer.xTransformer.wrapCoordToLimit(x);
            super.setX(x);
            return this;
        }

        public BlockPosWrapped.@NotNull MutableBlockPos setZ(int z) {
            z = transformer.zTransformer.wrapCoordToLimit(z);
            super.setZ(z);
            return this;
        }
    }

    private BlockPos blockToBounds(BlockPos blockPos) {
        return transformer.translateBlockToBounds(blockPos);
    }

    private static BlockPos blockToBounds(BlockPos blockPos, WorldTransformer transformer) {
        return transformer.translateBlockToBounds(blockPos);
    }


}
