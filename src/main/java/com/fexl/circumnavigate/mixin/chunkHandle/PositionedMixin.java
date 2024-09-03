/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.chunkHandle;

import com.fexl.circumnavigate.storage.TransformerRequests;
import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.server.level.ChunkTrackingView.Positioned;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(Positioned.class)
public class PositionedMixin {
	private Positioned thiz = (Positioned) (Object) this;

	/**
	 * Modified to ensure when the player is on a chunk boundary, and login, they will get the correct chunks from both sides of the world. This method is only used on login.
	 */
	@Inject(method = "forEach", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void forEach(Consumer<ChunkPos> action, CallbackInfo ci) {
		ci.cancel();
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();

		List<Vec2> coords = new ArrayList<Vec2>();
		
		for(int x = thiz.minX(); x <= thiz.maxX(); x++) {
			for(int z = thiz.minZ(); z <= thiz.maxZ(); z++) {
				coords.add(new Vec2(transformer.xTransformer.wrapChunkToLimit(x), transformer.zTransformer.wrapChunkToLimit(z)));
			}
		}

		for(Vec2 coord : coords) {
			if(!thiz.contains((int)coord.x, (int)coord.y)) {
				continue;
			}
			action.accept(new ChunkPos((int)coord.x, (int)coord.y));
		}


	}

	/**
	 * Modified to ensure chunks aren't unnecessarily sent or dropped when the player is teleported across the world bounds.
	 */
	@Inject(method = "squareIntersects(Lnet/minecraft/server/level/ChunkTrackingView$Positioned;)Z", at = @At("HEAD"), cancellable = true)
	protected void squareIntersects(Positioned other, CallbackInfoReturnable<Boolean> cir) {
		WorldTransformer transformer = TransformerRequests.chunkMapLevel.getTransformer();
		boolean xIntersects = (thiz.minX() <= other.maxX() && thiz.maxX() >= other.minX()) ||
			(thiz.minX() + transformer.xWidth <= other.maxX() && thiz.maxX() + transformer.xWidth >= other.minX()) ||
			(thiz.minX() <= other.maxX() + transformer.xWidth && thiz.maxX() >= other.minX() + transformer.xWidth);

		boolean zIntersects = (thiz.minZ() <= other.maxZ() && thiz.maxZ() >= other.minZ()) ||
			(thiz.minZ() + transformer.zWidth <= other.maxZ() && thiz.maxZ() + transformer.zWidth >= other.minZ()) ||
			(thiz.minZ() <= other.maxZ() + transformer.zWidth && thiz.maxZ() >= other.minZ() + transformer.zWidth);

		cir.setReturnValue(xIntersects && zIntersects);
	}
}
