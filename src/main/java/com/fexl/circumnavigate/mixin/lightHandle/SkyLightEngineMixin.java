package com.fexl.circumnavigate.mixin.lightHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkyLightEngine.class)
public class SkyLightEngineMixin {
	@Unique
	BlockGetter level = ((LightEngineAccessor) this).getChunkSource().getLevel();

    @ModifyVariable(method = "propagateIncrease", at = @At("HEAD"), index = 1, argsOnly = true)
    public long wrapBlockPos1(long pos) {
		if(level.isClientSide()) return pos;
        WorldTransformer transformer = level.getTransformer();
        return transformer.translateBlockToBounds(pos);
    }

    @Redirect(method = "propagateIncrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(JLnet/minecraft/core/Direction;)J"))
    public long wrapBlockPos2(long pos, Direction direction) {
	    if(level.isClientSide()) return BlockPos.offset(pos, direction);
        WorldTransformer transformer = level.getTransformer();
        return transformer.translateBlockToBounds(BlockPos.offset(pos, direction));
    }

    @ModifyVariable(method = "propagateDecrease", at = @At("HEAD"), index = 1, argsOnly = true)
    public long wrapBlockPos3(long pos) {
	    if(level.isClientSide()) return pos;
        WorldTransformer transformer = level.getTransformer();
        return transformer.translateBlockToBounds(pos);
    }

    @Redirect(method = "propagateDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(JLnet/minecraft/core/Direction;)J"))
    public long wrapBlockPos4(long pos, Direction direction) {
		if(level.isClientSide()) return BlockPos.offset(pos, direction);
        WorldTransformer transformer = level.getTransformer();
        return transformer.translateBlockToBounds(BlockPos.offset(pos, direction));
    }

}
