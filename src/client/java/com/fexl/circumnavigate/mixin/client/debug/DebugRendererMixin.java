/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.client.debug;

import static com.fexl.circumnavigate.client.storage.DebugVariables.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Shadow @Final public DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
	@Shadow @Final public StructureRenderer structureRenderer;
	@Shadow @Final public PathfindingRenderer pathfindingRenderer;
	@Shadow @Final public DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
	@Shadow @Final public DebugRenderer.SimpleDebugRenderer chunkRenderer;

	@Inject(method = "render", at = @At("HEAD"))
	public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
		if(renderNeighborUpdates) neighborsUpdateRenderer.render(poseStack, bufferSource, camX, camY, camZ);
		if(renderStructurePieces) structureRenderer.render(poseStack, bufferSource, camX, camY, camZ);
		if(renderPathfinding) pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
		if(renderLightDebug) lightDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
		if(renderChunkInfo) chunkRenderer.render(poseStack, bufferSource, camX, camY, camZ);
	}

}
