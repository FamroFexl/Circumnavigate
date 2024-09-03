/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.chunkHandle;

import net.minecraft.server.level.ChunkTrackingView.Positioned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Positioned.class)
public abstract interface PositionedAccessorMixin {
	@Invoker("squareIntersects") abstract boolean squareIntersectsAM(Positioned other);

}
