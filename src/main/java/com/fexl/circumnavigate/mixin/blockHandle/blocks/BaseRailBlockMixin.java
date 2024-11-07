package com.fexl.circumnavigate.mixin.blockHandle.blocks;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BaseRailBlock.class)
public class BaseRailBlockMixin {

    @ModifyVariable(method = "neighborChanged", at = @At("HEAD"), index = 3, argsOnly = true)
    public BlockPos neighborChanged(BlockPos blockPos, @Local(argsOnly = true) Level level) {
        level.getBlockState(blockPos).getBlock();
        if(level.isClientSide) return blockPos;
        return level.getTransformer().translateBlockToBounds(blockPos);
    }


}
