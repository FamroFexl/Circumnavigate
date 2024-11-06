package com.fexl.circumnavigate.mixin.entityHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fix knockback miscalculation
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique private double x;
    @Unique private double z;

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(name = "entity2") Entity enemy) {
        Entity thiz = (Entity) (Object) this;
        WorldTransformer transformer = enemy.level().getTransformer();
        x = transformer.xTransformer.getActualDistanceTo(thiz.getX(), enemy.getX());
        z = transformer.zTransformer.getActualDistanceTo(thiz.getZ(), enemy.getZ());
    }

    @WrapMethod(method = "knockback")
    public void knockback(double strength, double x, double z, Operation<Void> original) {
        original.call(strength, this.x, this.z);
    }

    @WrapMethod(method = "indicateDamage")
    public void indicateDamage(double xDistance, double zDistance, Operation<Void> original) {
        original.call(this.x, this.z);
    }
}
