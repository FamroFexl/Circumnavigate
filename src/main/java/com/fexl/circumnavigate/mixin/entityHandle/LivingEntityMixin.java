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

    @Unique private double deltaX;
    @Unique private double deltaZ;

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(name = "entity2") Entity enemy) {
        Entity thiz = (Entity) (Object) this;
        WorldTransformer transformer = enemy.level().getTransformer();
        deltaX = transformer.xTransformer.getMinimumDistBetween(thiz.getX(), enemy.getX());
        deltaZ = transformer.zTransformer.getMinimumDistBetween(thiz.getZ(), enemy.getZ());

        // Vanilla parity code
        while (deltaX * deltaX + deltaZ * deltaZ < 1.0E-4) {
            deltaX = (Math.random() - Math.random()) * 0.01;
            deltaZ = (Math.random() - Math.random()) * 0.01;
        }
    }

    @WrapMethod(method = "knockback")
    public void knockback(double strength, double x, double z, Operation<Void> original) {
        // In case this method would be called from somewhere else
        if (deltaX != 0 || deltaZ != 0) {
            x = deltaX;
            z = deltaZ;
        }

        original.call(strength, x, z);
    }

    @WrapMethod(method = "indicateDamage")
    public void indicateDamage(double x, double z, Operation<Void> original) {
        // In case this method would be called from somewhere else
        if (deltaX != 0 || deltaZ != 0) {
            x = deltaX;
            z = deltaZ;
        }

        original.call(x, z);
    }
}
