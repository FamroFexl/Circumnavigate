package com.fexl.circumnavigate.mixin.entity;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Mob.class)
public abstract class MobMixin {

    @ModifyVariable(method = "lookAt", at = @At(value = "STORE"), ordinal = 0, slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getEyeY()D")))
    public double modifyD(double d, @Local(argsOnly = true) Entity entity) {
        Mob thiz = (Mob) (Object) this;
        DimensionTransformer transformer = thiz.level().getTransformer().onlyServerSide();

        return transformer.Coord.X.deltaFromBounds(thiz.getX(), entity.getX());
    }

    @ModifyVariable(method = "lookAt", at = @At(value = "STORE"), ordinal = 1, slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getEyeY()D")))
    public double modifyE(double e, @Local(argsOnly = true) Entity entity) {
        Mob thiz = (Mob) (Object) this;
        DimensionTransformer transformer = thiz.level().getTransformer().onlyServerSide();

        return transformer.Coord.Z.deltaFromBounds(thiz.getZ(), entity.getZ());
    }

    @ModifyReturnValue(method = "isWithinMeleeAttackRange", at = @At("RETURN"))
    public boolean modifyReturnValue(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        if (original) { // we dont need to revalidate it if its already true
            return true;
        }

        Mob thiz = (Mob) (Object) this;
        Level level = thiz.level();
        DimensionTransformer transformer = level.getTransformer().onlyServerSide();

        return transformer.AABB.unwrapFromBounds(thiz.getBoundingBox(), entity.getHitbox()).intersects(thiz.getBoundingBox());
    }
}
