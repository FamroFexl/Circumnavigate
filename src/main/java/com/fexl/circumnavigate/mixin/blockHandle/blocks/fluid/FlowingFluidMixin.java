package com.fexl.circumnavigate.mixin.blockHandle.blocks.fluid;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @WrapOperation(method = "getSpread", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos getWrapSpread(BlockPos instance, Direction direction, Operation<BlockPos> original, @Local(argsOnly = true) Level level) {
        WorldTransformer transformer = level.getTransformer();
        BlockPos result = original.call(instance, direction);
        return transformer.translateBlockToBounds(result);
    }

    @WrapMethod(method = "getSlopeDistance")
    private int wrapSlopeDistance(LevelReader level, BlockPos spreadPos, int distance, Direction direction, BlockState currentSpreadState, BlockPos sourcePos, Short2ObjectMap<Pair<BlockState, FluidState>> stateCache, Short2BooleanMap waterHoleCache, Operation<Integer> original) {
        BlockPos wrappedSpreadPos = level.getTransformer().translateBlockToBounds(spreadPos);
        BlockPos wrappedSourcePos = level.getTransformer().translateBlockToBounds(sourcePos);
        return original.call(level, wrappedSpreadPos, distance, direction, currentSpreadState, wrappedSourcePos, stateCache, waterHoleCache);
    }

    @WrapOperation(method = "spreadToSides", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos wrapSpreadToSides(BlockPos instance, Direction direction, Operation<BlockPos> original, @Local(argsOnly = true) Level level) {
        WorldTransformer transformer = level.getTransformer();
        BlockPos result = original.call(instance, direction);
        return transformer.translateBlockToBounds(result);
    }
}
