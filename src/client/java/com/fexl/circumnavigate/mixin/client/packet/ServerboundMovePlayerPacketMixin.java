/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.packet;

import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerboundMovePlayerPacket.class)
public class ServerboundMovePlayerPacketMixin {

	@ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static double modifyX(double x) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();
		return transformer.xTransformer.wrapCoordToLimit(x);
	}

	@ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private static double modifyZ(double z) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();
		return transformer.zTransformer.wrapCoordToLimit(z);
	}
}
