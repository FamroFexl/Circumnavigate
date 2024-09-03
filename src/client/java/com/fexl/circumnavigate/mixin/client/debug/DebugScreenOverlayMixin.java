/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.client.debug;

import com.fexl.circumnavigate.util.WorldTransformer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@Shadow Minecraft minecraft;

	@Inject(method = "getGameInformation()Ljava/util/List;", at = @At("RETURN"))
	public void getGameInformation(CallbackInfoReturnable<List<String>> cir, @Local BlockPos blockPos, @Local ChunkPos chunkPos, @Local List list) {
		WorldTransformer transformer = minecraft.level.getTransformer();
		if(!transformer.isWrapped())
			return;

		int addPos;
		if(list.get(9).equals(""))
			addPos = 13;
		else
			addPos = 12;

		//Provides the server-side chunk & block position if the player is past the range where they are identical
		if(transformer.xTransformer.isCoordOverLimit(blockPos.getX()) || transformer.zTransformer.isCoordOverLimit(blockPos.getZ())) {
			list.add(addPos++, String.format(Locale.ROOT, "Actual Block: %d %d %d", transformer.xTransformer.wrapCoordToLimit(blockPos.getX()), blockPos.getY(), transformer.zTransformer.wrapCoordToLimit(blockPos.getZ())));
			list.add(addPos++, String.format(Locale.ROOT, "Actual Chunk: %d %d %d", transformer.xTransformer.wrapChunkToLimit(chunkPos.x), SectionPos.blockToSectionCoord((int)blockPos.getY()), transformer.zTransformer.wrapChunkToLimit(chunkPos.z)));
		}
		//Shows the wrapping info
		list.add(addPos, String.format(Locale.ROOT, "Wrapping: MinX: %d, MaxX: %d, MinZ: %d, MaxZ: %d" + ((transformer.xShift != 0 || transformer.zShift != 0) ? ", Axis: %s, Shift: %d" : ""), transformer.xChunkBoundMin, transformer.xChunkBoundMax, transformer.zChunkBoundMin, transformer.zChunkBoundMax, (transformer.xShift != 0) ? "X" : "Y", (transformer.xShift != 0) ? transformer.xShift : transformer.zShift));

	}
}
