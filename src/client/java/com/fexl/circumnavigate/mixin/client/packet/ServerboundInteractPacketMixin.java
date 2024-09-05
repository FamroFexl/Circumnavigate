/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.packet;

import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerboundInteractPacket.class)
public class ServerboundInteractPacketMixin {
	@ModifyVariable(method = "createInteractionPacket(Lnet/minecraft/world/entity/Entity;ZLnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;", at = @At("HEAD"), index = 3)
	private static Vec3 modifyInteractionLocation(Vec3 interactionLocation) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();
		return transformer.translateVecToBounds(interactionLocation);
	}
}
