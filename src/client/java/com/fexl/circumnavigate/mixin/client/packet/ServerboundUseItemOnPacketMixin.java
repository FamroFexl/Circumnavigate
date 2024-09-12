/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.packet;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerboundUseItemOnPacket.class)
public class ServerboundUseItemOnPacketMixin {
	@ModifyVariable(method = "<init>(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;I)V", at = @At("HEAD"), index = 2)
	private static BlockHitResult modifyHitResult(BlockHitResult blockHit) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();
		return new BlockHitResult(transformer.translateVecToBounds(blockHit.getLocation()), blockHit.getDirection(), transformer.translateBlockToBounds(blockHit.getBlockPos()), blockHit.isInside());
	}
}
