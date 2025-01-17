package com.fexl.circumnavigate.mixin.entity.entities.monster;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {

    @ModifyVariable(method = "performRangedAttack", at = @At("STORE"), name = "d")
    public double modifyD(double d, @Local(argsOnly = true) LivingEntity target) {
        Mob thiz = (Mob) (Object) this;
        DimensionTransformer transformer = thiz.level().getTransformer().onlyServerSide();

        return transformer.Coord.X.deltaFromBounds(thiz.getX(), target.getX());
    }

    @ModifyVariable(method = "performRangedAttack", at = @At("STORE"), name = "f")
    public double modifyF(double f, @Local(argsOnly = true) LivingEntity target) {
        Mob thiz = (Mob) (Object) this;
        DimensionTransformer transformer = thiz.level().getTransformer().onlyServerSide();

        return transformer.Coord.Z.deltaFromBounds(thiz.getZ(), target.getZ());
    }
}
