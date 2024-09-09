/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.debug;

import com.fexl.circumnavigate.util.WorldTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes the debug chunk borders so that they appear purple at the world borders.
 */
@Mixin(ChunkBorderRenderer.class)
public class ChunkBorderRendererMixin {
	private static final int CELL_BORDER = FastColor.ARGB32.color((int)255, (int)0, (int)155, (int)155);
	private static final int YELLOW = FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)0);

	private static final int DARK_PURPLE = FastColor.ARGB32.color((int)255, (int)75, (int)0, (int)130);
	private static final int PURPLE = FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)255);
	private static final int PURPLE_CLEAR = FastColor.ARGB32.color((int)0, (int)255, (int)0, (int)255);

	//TODO: computationally expensive?
	private boolean onBounds(int chunkPos, int iter, int bounds, int width) {
		if(bounds == 0) {
			return chunkPos == 0 && iter == 0;
		}
		return (chunkPos + iter/16) % (width) == bounds;
	}

	//TODO doesn't work in worlds where the min and max bounds aren't opposites (i.g. -32 -> 32)
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void render(PoseStack poseStack, MultiBufferSource buffer, double camX, double camY, double camZ, CallbackInfo ci) {
		ChunkBorderRenderer thiz = (ChunkBorderRenderer) (Object) this;

		ChunkBorderRendererAccessorMixin am = (ChunkBorderRendererAccessorMixin) (Object) thiz;

		WorldTransformer transformer = am.getMinecraft().level.getTransformer();

		ci.cancel();

		int k;
		int j;
		Entity entity = am.getMinecraft().gameRenderer.getMainCamera().getEntity();
		float f = (float)((double)am.getMinecraft().level.getMinBuildHeight() - camY);
		float g = (float)((double)am.getMinecraft().level.getMaxBuildHeight() - camY);
		ChunkPos chunkPos = entity.chunkPosition();
		float h = (float)((double)chunkPos.getMinBlockX() - camX);
		float i = (float)((double)chunkPos.getMinBlockZ() - camZ);
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
		Matrix4f matrix4f = poseStack.last().pose();

		//Red lines showing distant chunk borders
		for (j = -16; j <= 32; j += 16) {
			for (k = -16; k <= 32; k += 16) {
				vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
				//Set purple
				if(onBounds(chunkPos.x, j, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x, j, transformer.xChunkBoundMax, transformer.xWidth) || onBounds(chunkPos.z, k, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z, k, transformer.zChunkBoundMax, transformer.zWidth)) {
					vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k).color(DARK_PURPLE).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k).color(DARK_PURPLE).endVertex();
				}
				//Set red
				else {
					vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
				}
				vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
			}
		}

		//Yellow/green vertical for north and south
		for (j = 2; j < 16; j += 2) {
			k = j % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.vertex(matrix4f, h + (float)j, f, i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, f, i).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, g, i).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, g, i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, f, i + 16.0f).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, f, i + 16.0f).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, g, i + 16.0f).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h + (float)j, g, i + 16.0f).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
		}

		//Yellow/green vertical for east and west
		for (j = 2; j < 16; j += 2) {
			k = j % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.vertex(matrix4f, h, f, i + (float)j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h, f, i + (float)j).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h, g, i + (float)j).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h, g, i + (float)j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h + 16.0f, f, i + (float)j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h + 16.0f, f, i + (float)j).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h + 16.0f, g, i + (float)j).color(k).endVertex();
			vertexConsumer.vertex(matrix4f, h + 16.0f, g, i + (float)j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
		}

		//Yellow/green horizontal for all directions
		for (j = am.getMinecraft().level.getMinBuildHeight(); j <= am.getMinecraft().level.getMaxBuildHeight(); j += 2) {
			float l = (float)((double)j - camY);
			int m = j % 8 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.vertex(matrix4f, h, l, i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
			vertexConsumer.vertex(matrix4f, h, l, i).color(m).endVertex();
			vertexConsumer.vertex(matrix4f, h, l, i + 16.0f).color(m).endVertex();
			vertexConsumer.vertex(matrix4f, h + 16.0f, l, i + 16.0f).color(m).endVertex();
			vertexConsumer.vertex(matrix4f, h + 16.0f, l, i).color(m).endVertex();
			vertexConsumer.vertex(matrix4f, h, l, i).color(m).endVertex();
			vertexConsumer.vertex(matrix4f, h, l, i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
		}

		vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(2.0));

		//Blue vertical for immediate chunk corners
		for (j = 0; j <= 16; j += 16) {
			for (int k2 = 0; k2 <= 16; k2 += 16) {
				//Set purple
				if(onBounds(chunkPos.x, j, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x, j, transformer.xChunkBoundMax, transformer.xWidth) || onBounds(chunkPos.z, k2, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z, k2, transformer.zChunkBoundMax, transformer.zWidth)) {vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k2).color(PURPLE_CLEAR).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k2).color(PURPLE).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k2).color(PURPLE).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k2).color(PURPLE_CLEAR).endVertex();
				}
				//Set red
				else {
					vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k2).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, f, i + (float)k2).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k2).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
					vertexConsumer.vertex(matrix4f, h + (float)j, g, i + (float)k2).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
				}
			}
		}

		//Blue horizontal for chunk segment borders
		for (j = am.getMinecraft().level.getMinBuildHeight(); j <= am.getMinecraft().level.getMaxBuildHeight(); j += 16) {
			float l = (float)((double)j - camY);

			vertexConsumer.vertex(matrix4f, h, l, i).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();

			//Northwest to southwest
			if(onBounds(chunkPos.x, 0, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x, 0, transformer.xChunkBoundMax, transformer.xWidth)) {
				vertexConsumer.vertex(matrix4f, h, l, i).color(PURPLE).endVertex();
				vertexConsumer.vertex(matrix4f, h, l, i + 16.0f).color(PURPLE).endVertex();
			}
			else {
				vertexConsumer.vertex(matrix4f, h, l, i).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
				vertexConsumer.vertex(matrix4f, h, l, i + 16.0f).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
			}

			//Southwest to southeast
			if(onBounds(chunkPos.z + 1, 0, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z + 1, 0, transformer.zChunkBoundMax, transformer.zWidth)) {
				vertexConsumer.vertex(matrix4f, h, l, i + 16.0f).color(PURPLE).endVertex();
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i + 16.0f).color(PURPLE).endVertex();
			}
			else {
				vertexConsumer.vertex(matrix4f, h, l, i + 16.0f).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i + 16.0f).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
			}

			//Southeast to northeast
			if(onBounds(chunkPos.x + 1, 0, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x + 1, 0, transformer.xChunkBoundMax, transformer.xWidth)) {
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i + 16.0f).color(PURPLE).endVertex();
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i).color(PURPLE).endVertex();
			}
			else {
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i + 16.0f).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
			}

			//Northeast to northwest
			if(onBounds(chunkPos.z, 0, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z, 0, transformer.zChunkBoundMax, transformer.zWidth)) {
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i).color(PURPLE).endVertex();
				vertexConsumer.vertex(matrix4f, h, l, i).color(PURPLE).endVertex();
			}
			else {
				vertexConsumer.vertex(matrix4f, h + 16.0f, l, i).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
				vertexConsumer.vertex(matrix4f, h, l, i).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
			}

			vertexConsumer.vertex(matrix4f, h, l, i).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
		}
	}
}
