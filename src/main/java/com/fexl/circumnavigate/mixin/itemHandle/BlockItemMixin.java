/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.itemHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
	public void placeBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if(context.getLevel().isClientSide) return;
		WorldTransformer transformer = context.getLevel().getTransformer();
		cir.setReturnValue(context.getLevel().setBlock(transformer.translateBlockToBounds(context.getClickedPos()), state, 11));
	}
}
