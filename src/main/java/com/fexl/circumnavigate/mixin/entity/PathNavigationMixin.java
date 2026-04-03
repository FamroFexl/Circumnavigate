package com.fexl.circumnavigate.mixin.entity;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {
	@Shadow @Final public Mob mob;
	@Shadow @Final public Level level;
	@Shadow protected @Nullable Path path;

	@WrapMethod(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;")
	public @Nullable Path wrapCreatePath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange, Operation<Path> original) {
		BlockPos thizPos = this.mob.blockPosition();
		DimensionTransformer transformer = getTransformer();
		// Check each target if its in different wrapped space (out-of-bounds) if not then pass as is
		// if so then get delta though the bounds and append it to the thizPos and pass that as a target pos

		HashSet<BlockPos> newTargets = new HashSet<>();

		targets.forEach(target -> {
			newTargets.add(transformer.Block.unwrap(thizPos, target).equals(target) ? target :
					transformer.Block.deltaFromBounds(thizPos, target).offset(thizPos.getX(), 0, thizPos.getZ()));
		});

		return original.call(newTargets, regionOffset, offsetUpward, accuracy, followRange);
	}

	// After a wrap crossing, the mob is in a new coordinate frame but the cached path nodes are not.
	// Rebase the remaining nodes into the mob's current wrapped space before vanilla follows them.
	// This preserves the existing path instead of making navigation/stuck checks use stale coordinates.
	@Inject(method = "followThePath", at = @At("HEAD"))
	private void normalizePathNodesBeforeFollowing(CallbackInfo ci) {
		if (this.path == null || this.path.isDone()) {
			return;
		}

		BlockPos referencePos = this.mob.blockPosition();
		BlockPos nextNodePos = this.path.getNextNodePos();
		BlockPos unwrappedNextNodePos = getTransformer().Block.unwrap(referencePos, nextNodePos);

		if (nextNodePos.equals(unwrappedNextNodePos)) {
			return;
		}

		for (int i = this.path.getNextNodeIndex(); i < this.path.getNodeCount(); i++) {
			Node node = this.path.getNode(i);
			BlockPos unwrappedNodePos = getTransformer().Block.unwrap(referencePos, node.asBlockPos());

			if (node.x != unwrappedNodePos.getX() || node.y != unwrappedNodePos.getY() || node.z != unwrappedNodePos.getZ()) {
				this.path.replaceNode(i, node.cloneAndMove(unwrappedNodePos.getX(), unwrappedNodePos.getY(), unwrappedNodePos.getZ()));
			}

			referencePos = unwrappedNodePos;
		}
	}

	@Unique
	private DimensionTransformer getTransformer() {
		return this.level.getTransformer().SSO();
	}
}
