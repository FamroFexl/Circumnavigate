/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.chunk.propagators;

import com.fexl.circumnavigate.accessors.TransformerAccessor;
import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.mixin.chunk.SectionTrackerMixin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.ai.village.poi.PoiManager$DistanceTracker")
public class PoiManager$DistanceTracker extends SectionTrackerMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(PoiManager poiManager, CallbackInfo ci) {
		//This is only initialized in ChunkMap with a ServerLevel, which is why we can assume it is a ServerLevel
		WorldTransformer transformer = ((ServerLevel)((SectionStorage<PoiSection>) poiManager).levelHeightAccessor).getTransformer();
		((TransformerAccessor) this).setTransformer(transformer);
	}
}
