package com.fexl.circumnavigate.mixin.entity;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	LivingEntity thiz = (LivingEntity) (Object) this;

    // Fix knockback miscalculation
    @Unique private double knockbackDeltaX;
    @Unique private double knockbackDeltaZ;

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    public void wrapDelta(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity thiz = (Entity) (Object) this;
        Entity enemy = source.getEntity();
        DimensionTransformer transformer = enemy.level().getTransformer().onlyServerSide();
        knockbackDeltaX = transformer.Coord.X.deltaFromBounds(thiz.getX(), enemy.getX());
        knockbackDeltaZ = transformer.Coord.Z.deltaFromBounds(thiz.getZ(), enemy.getZ());

        // Vanilla parity code
        while (knockbackDeltaX * knockbackDeltaX + knockbackDeltaZ * knockbackDeltaZ < 1.0E-4) {
            knockbackDeltaX = (Math.random() - Math.random()) * 0.01;
            knockbackDeltaZ = (Math.random() - Math.random()) * 0.01;
        }
    }

    @WrapMethod(method = "knockback")
    public void wrapDistance1(double strength, double x, double z, Operation<Void> original) {
        // In case this method would be called from somewhere else
        if (knockbackDeltaX != 0 || knockbackDeltaZ != 0) {
            x = knockbackDeltaX;
            z = knockbackDeltaZ;
        }

        original.call(strength, x, z);
    }

    @WrapMethod(method = "indicateDamage")
    public void wrapDistance2(double x, double z, Operation<Void> original) {
        // In case this method would be called from somewhere else
        if (knockbackDeltaX != 0 || knockbackDeltaZ != 0) {
            x = knockbackDeltaX;
            z = knockbackDeltaZ;
        }

        original.call(x, z);
    }

	@Redirect(method = "hasLineOfSight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"))
	public double modifyDistTo(Vec3 instance, Vec3 vec) {
		return thiz.level().getTransformer().Vector3D.unwrapFromBounds(vec, instance).distanceTo(vec);
	}
}
