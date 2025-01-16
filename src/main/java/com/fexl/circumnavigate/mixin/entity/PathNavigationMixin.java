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

        targets.forEach(target -> {
            newTargets.add(transformer.Block.unwrapFromBounds(thizPos, target).equals(target) ? target :
                    transformer.Block.deltaFromBounds(thizPos, target).offset(thizPos.getX(), 0, thizPos.getZ()));
        });

        return original.call(newTargets, regionOffset, offsetUpward, accuracy, followRange);
    }
}
