/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.chunkHandle;

import net.minecraft.server.level.ChunkTrackingView.Positioned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Positioned.class)
public abstract interface PositionedAccessorMixin {
	@Invoker("squareIntersects") abstract boolean squareIntersectsAM(Positioned other);

}
