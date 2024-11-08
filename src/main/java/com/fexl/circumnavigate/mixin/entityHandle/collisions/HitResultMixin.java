package com.fexl.circumnavigate.mixin.entityHandle.collisions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HitResult.class)
public class HitResultMixin {
    @Final @Shadow protected Vec3 location;
    @Inject(method = "distanceTo", at = @At("HEAD"), cancellable = true)
    public void wrapDistanceSquared(Entity entity, CallbackInfoReturnable<Double> cir) {
        if(entity.level().isClientSide) return;
        cir.setReturnValue(entity.level().getTransformer().distanceToSqrWrapped(entity.getX(), entity.getY(), entity.getZ(), location.x, location.y, location.z));
    }
}
