/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packetHandle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class ServerGamePacketListenerImplMixin_1 {
	@ModifyArg(method = "method_33898", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), index = 1)
	private static Vec3 handleInteract_onInteraction(Player player, Vec3 vec, InteractionHand interactionHand) {
		return player.level().getTransformer().translateVecToBounds(vec);
	}
}
