package com.fexl.circumnavigate.mixin.entityHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    // Fix knockback miscalculation
    @WrapOperation(method = "pullEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    public void pullEntity(Entity instance, Vec3 deltaMovement, Operation<Void> original, @Local(name = "entity2") Entity owner) {
        WorldTransformer transformer = instance.level().getTransformer();
        double x = transformer.xTransformer.getActualDistanceTo(instance.getX(), owner.getX());
        double z = transformer.zTransformer.getActualDistanceTo(instance.getZ(), owner.getZ());
        Vec3 newDeltaMovement = new Vec3(x, deltaMovement.y, z).scale(0.1);
        System.out.println("FishingHookMixin.pullEntity from: " + deltaMovement + " to: " + newDeltaMovement);
        original.call(instance, instance.getDeltaMovement().add(newDeltaMovement));
    }
}
