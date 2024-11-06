package com.fexl.circumnavigate.mixin.lightHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockLightEngine.class)
public class BlockLightEngineMixin {
    @ModifyVariable(method = "propagateIncrease", at = @At("HEAD"), index = 1, argsOnly = true)
    public long propagateIncrease(long pos) {
        WorldTransformer transformer = ((LightEngineAccessor) this).getChunkSource().getLevel().getTransformer();
        return transformer.translateBlockToBounds(pos);
    }

    @Redirect(method = "propagateIncrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(JLnet/minecraft/core/Direction;)J"))
    public long changeOffset(long pos, Direction direction) {
        WorldTransformer transformer = ((LightEngineAccessor) this).getChunkSource().getLevel().getTransformer();
        return transformer.translateBlockToBounds(BlockPos.offset(pos, direction));
    }

    @Redirect(method = "propagateDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(JLnet/minecraft/core/Direction;)J"))
    public long changeOffset2(long pos, Direction direction) {
        WorldTransformer transformer = ((LightEngineAccessor) this).getChunkSource().getLevel().getTransformer();
        return transformer.translateBlockToBounds(BlockPos.offset(pos, direction));
    }
}
