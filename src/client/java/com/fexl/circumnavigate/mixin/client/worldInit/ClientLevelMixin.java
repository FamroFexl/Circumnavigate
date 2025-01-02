/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.worldInit;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import com.fexl.circumnavigate.core.DimensionTransformer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	ClientLevel thiz = (ClientLevel) (Object) this;

	/**
	 * Set the dimension transformer for the ClientLevel depending on the requested dimension.
	 */
	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(ClientPacketListener connection, ClientLevel.ClientLevelData clientLevelData, ResourceKey<Level> dimension, Holder dimensionType, int viewDistance, int serverSimulationDistance, Supplier profiler, LevelRenderer levelRenderer, boolean isDebug, long biomeZoomSeed, CallbackInfo ci) {
		thiz.setTransformer(TransformersStorage.getTransformer(dimension));
	}

	@ModifyVariable(method = "<init>", at = @At("HEAD"), index = 5, argsOnly = true)
	private static int changeViewDistance(int viewDistance, @Local ResourceKey dimension) {
		DimensionTransformer transformer = TransformersStorage.getTransformer(dimension);
		return transformer.limitViewDistance(viewDistance);
	}
}
