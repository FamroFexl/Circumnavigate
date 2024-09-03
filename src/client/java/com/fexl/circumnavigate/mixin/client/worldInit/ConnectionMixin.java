package com.fexl.circumnavigate.mixin.client.worldInit;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
	/**
	 * Set the world transformers to null, so they can be adjusted for the next world.
	 */
	@Inject(method = "disconnect", at = @At("HEAD"))
	public void disconnect(Component message, CallbackInfo ci) {
		TransformersStorage.setTransformers(null);
	}
}
