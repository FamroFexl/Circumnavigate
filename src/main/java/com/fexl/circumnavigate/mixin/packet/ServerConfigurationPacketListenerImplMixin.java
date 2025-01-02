/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packet;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.network.packet.DimensionWrappingPayload;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
	ServerConfigurationPacketListenerImpl thiz = (ServerConfigurationPacketListenerImpl) (Object) this;
	@Inject(method = "startConfiguration", at = @At("HEAD"))
	public void startConfiguration(CallbackInfo ci) {
		for(ServerLevel level : thiz.server.getAllLevels()) {
			if (level.getTransformer().equals(DimensionTransformer.DISABLED)) {
				continue;
			}
			ServerConfigurationNetworking.send(thiz, new DimensionWrappingPayload(level.dimension(), level.getTransformer().wrappingSettings));
		}
	}
}
