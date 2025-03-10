/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
	/**
	 * ChunkTrackingView is mainly used server-side. This is the only usage of it client-side. Because the server and client do not handle chunks in the same way, they cannot use the same code.
	 */
	@Inject(method = "isInViewDistance", at = @At("HEAD"))
	public void isInViewDistance(BlockPos pos, BlockPos origin, CallbackInfoReturnable<Boolean> cir) {
		TransformerRequests.chunkMapTransformer = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getTransformer() : DimensionTransformer.DISABLED;
	}
}
