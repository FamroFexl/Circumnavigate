package com.fexl.circumnavigate.mixin.entity;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashSet;
import java.util.Set;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {

    @WrapMethod(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;")
    public @Nullable Path wrapCreatePath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange, Operation<Path> original) {
        BlockPos thizPos = ((PathNavigation) (Object) this).mob.blockPosition();
        DimensionTransformer transformer = ((PathNavigation) (Object) this).level.getTransformer();
        // Check each target if its in different wrapped space (out-of-bounds) if not then pass as is
        // if so then get delta though the bounds and append it to the thizPos and pass that as a target pos

        HashSet<BlockPos> newTargets = new HashSet<>();
        for (BlockPos target : targets) {
            BlockPos newTarget = transformer.Block.unwrapFromBounds(thizPos, target);

            // check if the new target is in the same space as the original target
            if (newTarget.getX() == target.getX() && newTarget.getZ() == target.getZ()) {
                newTargets.add(target);
                continue;
            }

            System.out.println("Wrapping path navigation target");

            double deltaX = transformer.Coord.X.deltaFromBounds((double) thizPos.getX(), (double) target.getX());
            double deltaZ = transformer.Coord.Z.deltaFromBounds((double) thizPos.getZ(), (double) target.getZ());

            System.out.println("Delta X: " + deltaX + " Delta Z: " + deltaZ);

            newTarget = new BlockPos((int) (thizPos.getX() + deltaX), target.getY(), (int) (thizPos.getZ() + deltaZ));

            System.out.println("Original target: " + target + " New target: " + newTarget);

            newTargets.add(newTarget);
        }

        return original.call(newTargets, regionOffset, offsetUpward, accuracy, followRange);
    }
}
