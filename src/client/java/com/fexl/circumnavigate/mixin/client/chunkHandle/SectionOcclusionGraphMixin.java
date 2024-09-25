/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.chunkHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.server.level.ChunkTrackingView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
	/**
	 * ChunkTrackingView is mainly used server-side. This is the only usage of it client-side. Because the server and client do not handle chunks in the same way, they cannot use the same code.
	 */
	@Redirect(method = "isInViewDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTrackingView;isInViewDistance(IIIII)Z"))
	public boolean isInViewDistance(int centerX, int centerZ, int viewDistance, int x, int z) {
		int i = Math.max(0, Math.abs(x - centerX) - 1);
		int j = Math.max(0, Math.abs(z - centerZ) - 1);
		long l = (long)Math.max(0, Math.max(i, j) - 0);
		long m = (long)Math.min(i, j);
		long n = m * m + l * l;
		int k = viewDistance * viewDistance;
		return n < (long)k;
	}
}
