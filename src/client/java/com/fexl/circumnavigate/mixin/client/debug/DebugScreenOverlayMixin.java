/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.debug;

import com.fexl.circumnavigate.core.DimensionTransformer;
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
		DimensionTransformer transformer = minecraft.level.getTransformer();
		if(!transformer.isWrapped())
			return;

		int addPos;
		if(list.get(9).equals(""))
			addPos = 13;
		else
			addPos = 12;

		//Provides the server-side chunk & block position if the player is past the range where they are identical
		if(transformer.Coord.X.isOver(blockPos.getX()) || transformer.Coord.Z.isOver(blockPos.getZ())) {
			list.add(addPos++, String.format(Locale.ROOT, "Actual Block: %d %d %d", transformer.Coord.X.wrap(blockPos.getX()), blockPos.getY(), transformer.Coord.Z.wrap(blockPos.getZ())));
			list.add(addPos++, String.format(Locale.ROOT, "Actual Chunk: %d %d %d", transformer.Chunk.X.wrap(chunkPos.x), SectionPos.blockToSectionCoord((int)blockPos.getY()), transformer.Chunk.Z.wrap(chunkPos.z)));
		}
		//Shows the wrapping info
		list.add(addPos, String.format(Locale.ROOT, transformer.toString()));

	}
}
