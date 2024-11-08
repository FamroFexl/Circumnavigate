package com.fexl.circumnavigate.mixin.blockHandle.blocks;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RailState.class)
public class RailStateMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, index = 2)
    private static BlockPos wrapBlockPos(BlockPos blockPos, @Local(argsOnly = true) Level level) {
        if(level.isClientSide) return blockPos;
        return new BlockPosWrapped(blockPos, level.getTransformer());
    }
}
