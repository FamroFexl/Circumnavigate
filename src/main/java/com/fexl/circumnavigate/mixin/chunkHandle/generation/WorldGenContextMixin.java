/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunkHandle.generation;

import com.fexl.circumnavigate.accessors.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldGenContext.class)
public class WorldGenContextMixin {

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(ServerLevel serverLevel, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ThreadedLevelLightEngine threadedLevelLightEngine, ProcessorHandle processorHandle, CallbackInfo ci) {
		((LevelAccessor) chunkGenerator).setLevel(serverLevel);
	}
}
