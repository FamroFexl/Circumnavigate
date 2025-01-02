package com.fexl.circumnavigate.mixin.light;

import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerChunkCache;
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
		if(level instanceof ServerChunkCache cache) {
			DimensionTransformer transformer = cache.getLevel().getTransformer();
			return transformer.Block.wrapToBounds(pos);
		}

		return pos;
    }

    @Redirect(method = "propagateIncrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(JLnet/minecraft/core/Direction;)J"))
    public long wrapBlockPos2(long pos, Direction direction) {
	    if(level instanceof ServerChunkCache cache) {
		    DimensionTransformer transformer = cache.getLevel().getTransformer();
		    return transformer.Block.wrapToBounds(BlockPos.offset(pos, direction));
	    }

	    return BlockPos.offset(pos, direction);
    }

    @ModifyVariable(method = "propagateDecrease", at = @At("HEAD"), index = 1, argsOnly = true)
    public long wrapBlockPos3(long pos) {
	    if(level instanceof ServerChunkCache cache) {
		    DimensionTransformer transformer = cache.getLevel().getTransformer();
		    return transformer.Block.wrapToBounds(pos);
	    }

	    return pos;
    }

    @Redirect(method = "propagateDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(JLnet/minecraft/core/Direction;)J"))
    public long wrapBlockPos4(long pos, Direction direction) {
	    if(level instanceof ServerChunkCache cache) {
		    DimensionTransformer transformer = cache.getLevel().getTransformer();
		    return transformer.Block.wrapToBounds(BlockPos.offset(pos, direction));
	    }

	    return BlockPos.offset(pos, direction);
    }

}
