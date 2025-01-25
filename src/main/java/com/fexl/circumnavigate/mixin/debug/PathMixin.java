/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Path.DebugData;
import net.minecraft.world.level.pathfinder.Target;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(Path.class)
public class PathMixin {
	@Shadow @Mutable @Nullable private Path.DebugData debugData;

	@Shadow @Final private List<Node> nodes;

	@Shadow @Final private BlockPos target;

	@Inject(method ="writeToStream", at = @At("HEAD"))
	private void writeToStream(FriendlyByteBuf buffer, CallbackInfo ci) {
		Node[] nodes1 = nodes.stream().filter(node -> !node.closed).toArray(Node[]::new);
		Node[] nodes2 = nodes.stream().filter(node -> node.closed).toArray(Node[]::new);
		this.debugData = new DebugData(nodes.stream().filter(node -> !node.closed).toArray(Node[]::new), nodes.stream().filter(node -> node.closed).toArray(Node[]::new), Set.of(new Target(target.getX(), target.getY(), target.getZ())));
	}
}
