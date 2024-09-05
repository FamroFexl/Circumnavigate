/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.chunkHandle;

import com.fexl.circumnavigate.util.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public class ClientChunkCacheMixin {

	ClientChunkCacheAccessorMixin accessor = (ClientChunkCacheAccessorMixin) (Object) this;
	/**
	 * Checks if the provided chunk is in range of the player view. Modified to support wrapped worlds.
	 */
	@Inject(method = "inRange(II)Z", at = @At("HEAD"), cancellable = true)
	public void inRange(int x, int z, CallbackInfoReturnable<Boolean> cir) {
		WorldTransformer transformer = Minecraft.getInstance().level.getTransformer();
		if(!transformer.isWrapped())
			return;

		int dx = Math.abs(x - accessor.getViewCenterX());
		int dz = Math.abs(z - accessor.getViewCenterZ());
		if(dx > transformer.xWidth / 2) {
			dx = transformer.xWidth - dx;
		}

		if(dz > transformer.zWidth / 2) {
			dz = transformer.zWidth - dz;
		}

		cir.setReturnValue(dx <= accessor.getChunkRadius() && dz <= accessor.getChunkRadius());
	}
}
