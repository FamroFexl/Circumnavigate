/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.block.blocks;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SignalGetter.class)
public interface SignalGetterMixin {

	@ModifyVariable(method = "getControlInputSignal", at = @At("HEAD"), index = 1, argsOnly = true)
	default BlockPos modifyBlockPos(BlockPos blockPos) {
		SignalGetter thiz = (SignalGetter) (Object) this;
		if(thiz instanceof ServerLevel serverLevel) {
			return new BlockPosWrapped(blockPos, serverLevel.getTransformer());
		}
		return blockPos;
	}

	@ModifyVariable(method = "getSignal", at = @At("HEAD"), index = 1, argsOnly = true)
	default BlockPos modifyBlockPos2(BlockPos blockPos) {
		SignalGetter thiz = (SignalGetter) (Object) this;
		if(thiz instanceof ServerLevel serverLevel) {
			return new BlockPosWrapped(blockPos, serverLevel.getTransformer());
		}
		return blockPos;
	}

	@ModifyVariable(method = "hasNeighborSignal", at = @At("HEAD"), index = 1, argsOnly = true)
	default BlockPos modifyBlockPos3(BlockPos blockPos) {
		SignalGetter thiz = (SignalGetter) (Object) this;
		if(thiz instanceof ServerLevel serverLevel) {
			return new BlockPosWrapped(blockPos, serverLevel.getTransformer());
		}
		return blockPos;
	}


}
