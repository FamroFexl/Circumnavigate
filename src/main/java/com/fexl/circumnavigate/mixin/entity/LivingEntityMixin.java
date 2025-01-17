package com.fexl.circumnavigate.mixin.entity;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

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

    // Required for entities to detect and attack players from wrapped space
    @ModifyReturnValue(method = "hasLineOfSight", at = @At(value = "RETURN"))
    public boolean hasLineOfSight(boolean original, @Local(argsOnly = true) Entity targetEntity) {
        if (original) { // if its already true we dont need to revalidate
            return true;
        }

        LivingEntity thiz = (LivingEntity) (Object) this;
        Level level = thiz.level();
        DimensionTransformer transformer = level.getTransformer().onlyServerSide();
        double deltaX = transformer.Coord.X.deltaFromBounds(thiz.getX(), targetEntity.getX());
        double deltaZ = transformer.Coord.Z.deltaFromBounds(thiz.getZ(), targetEntity.getZ());
        Vec3 thizVec = new Vec3(thiz.getX(), thiz.getY(), thiz.getZ());
        Vec3 targetEntityVecWrapped = thizVec.add(deltaX, 0, deltaZ); // add delta to the thiz vec and call it a day

        boolean nearEnough = !(targetEntityVecWrapped.distanceTo(thizVec) > 128.0);

        if (!nearEnough) {
            return false;
        }

        return level.clip(new ClipContext(thizVec, targetEntityVecWrapped, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, thiz)).getType() == BlockHitResult.Type.MISS;
    }
}
