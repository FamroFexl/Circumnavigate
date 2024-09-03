/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.client.packet;

import com.fexl.circumnavigate.util.WorldTransformer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerboundPlayerActionPacket.class)
public class ServerboundPlayerActionPacketMixin {
	@ModifyVariable(method = "<init>(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;I)V", at = @At("HEAD"), index = 2)
	private static BlockPos modifyBlockPos(BlockPos pos, @Local Action action) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();
		if(action.equals(Action.DROP_ALL_ITEMS) || action.equals(Action.DROP_ITEM) || action.equals(Action.RELEASE_USE_ITEM) || action.equals(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND)) {
			return pos;
		}
		return transformer.translateBlockToBounds(pos);
	}
}
