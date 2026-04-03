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

	@Unique
	// Tracks the previous follow tick so we only rebase nodes after the mob changes wrapped frame.
	private @Nullable BlockPos circumnavigate$lastFollowMobPos;

	@WrapMethod(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;")
	public @Nullable Path wrapCreatePath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange, Operation<Path> original) {
		BlockPos mobPos = this.mob.blockPosition();
		DimensionTransformer transformer = getTransformer();
		int mobX = mobPos.getX();
		int mobZ = mobPos.getZ();

		for (BlockPos target : targets) {
			if (transformer.Coord.X.needsUnwrap(mobX, target.getX()) || transformer.Coord.Z.needsUnwrap(mobZ, target.getZ())) {
				return original.call(circumnavigate$rebaseTargets(targets, transformer, mobX, mobZ), regionOffset, offsetUpward, accuracy, followRange);
			}
		}

		return original.call(targets, regionOffset, offsetUpward, accuracy, followRange);
	}

	// After a wrap crossing, the mob is in a new coordinate frame but the cached path nodes are not.
	// Rebase the remaining nodes into the mob's current wrapped space before vanilla follows them.
	// This preserves the existing path instead of making navigation/stuck checks use stale coordinates.
	@Inject(method = "followThePath", at = @At("HEAD"))
	private void normalizePathNodesBeforeFollowing(CallbackInfo ci) {
		Path path = this.path;
		if (path == null || path.isDone()) {
			return;
		}

		BlockPos mobPos = this.mob.blockPosition();
		BlockPos lastFollowMobPos = this.circumnavigate$lastFollowMobPos;
		this.circumnavigate$lastFollowMobPos = mobPos;

		DimensionTransformer transformer = getTransformer();
		if (!circumnavigate$shouldCheckPathFrame(lastFollowMobPos, mobPos, path, transformer)) {
			return;
		}

		circumnavigate$rebasePathNodes(path, mobPos, transformer);
	}

	@Unique
	private DimensionTransformer getTransformer() {
		return this.level.getTransformer().SSO();
	}

	@Unique
	private static boolean circumnavigate$shouldCheckPathFrame(@Nullable BlockPos lastFollowMobPos, BlockPos mobPos, Path path, DimensionTransformer transformer) {
		if (lastFollowMobPos != null) {
			if (mobPos.equals(lastFollowMobPos)) {
				return false;
			}

			if (!circumnavigate$didMobChangeWrappedFrame(lastFollowMobPos, mobPos, transformer)) {
				return false;
			}
		}

		return circumnavigate$nodeNeedsRebase(path.getNextNode(), mobPos, transformer);
	}

	@Unique
	private static boolean circumnavigate$didMobChangeWrappedFrame(BlockPos lastFollowMobPos, BlockPos mobPos, DimensionTransformer transformer) {
		return transformer.Coord.X.needsUnwrap(lastFollowMobPos.getX(), mobPos.getX())
			|| transformer.Coord.Z.needsUnwrap(lastFollowMobPos.getZ(), mobPos.getZ());
	}

	@Unique
	private static boolean circumnavigate$nodeNeedsRebase(Node node, BlockPos referencePos, DimensionTransformer transformer) {
		return transformer.Coord.X.needsUnwrap(referencePos.getX(), node.x)
			|| transformer.Coord.Z.needsUnwrap(referencePos.getZ(), node.z);
	}

	@Unique
	private static void circumnavigate$rebasePathNodes(Path path, BlockPos mobPos, DimensionTransformer transformer) {
		int referenceX = mobPos.getX();
		int referenceZ = mobPos.getZ();

		for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
			Node node = path.getNode(i);
			int unwrappedNodeX = transformer.Coord.X.unwrap(referenceX, node.x);
			int unwrappedNodeZ = transformer.Coord.Z.unwrap(referenceZ, node.z);

			if (node.x != unwrappedNodeX || node.z != unwrappedNodeZ) {
				path.replaceNode(i, node.cloneAndMove(unwrappedNodeX, node.y, unwrappedNodeZ));
			}

			referenceX = unwrappedNodeX;
			referenceZ = unwrappedNodeZ;
		}
	}

	@Unique
	private static Set<BlockPos> circumnavigate$rebaseTargets(Set<BlockPos> targets, DimensionTransformer transformer, int mobX, int mobZ) {
		HashSet<BlockPos> rebasedTargets = new HashSet<>(targets.size());

		for (BlockPos target : targets) {
			int rebasedX = transformer.Coord.X.unwrap(mobX, target.getX());
			int rebasedZ = transformer.Coord.Z.unwrap(mobZ, target.getZ());

			if (rebasedX == target.getX() && rebasedZ == target.getZ()) {
				rebasedTargets.add(target);
			} else {
				rebasedTargets.add(new BlockPos(rebasedX, target.getY(), rebasedZ));
			}
		}

		return rebasedTargets;
	}
}
