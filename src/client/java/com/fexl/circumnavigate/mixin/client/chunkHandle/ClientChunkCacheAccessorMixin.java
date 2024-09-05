/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.chunkHandle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public interface ClientChunkCacheAccessorMixin {
	@Accessor int getChunkRadius();

	@Accessor int getViewCenterX();

	@Accessor int getViewCenterZ();
}
