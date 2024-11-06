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
        double deltaX = transformer.xTransformer.getActualDistanceTo(instance.getX(), owner.getX());
        double deltaY = instance.getY() - owner.getY(); // Cant use y directly from deltaMovement since its scaled by vanilla
        double deltaZ = transformer.zTransformer.getActualDistanceTo(instance.getZ(), owner.getZ());

        Vec3 newDeltaMovement = new Vec3(deltaX, deltaY, deltaZ).scale(0.1);
        original.call(instance, instance.getDeltaMovement().add(newDeltaMovement));
    }
}
