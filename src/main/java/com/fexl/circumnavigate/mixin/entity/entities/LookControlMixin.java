package com.fexl.circumnavigate.mixin.entity.entities;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LookControl.class)
public abstract class LookControlMixin {

    @Shadow @Final protected Mob mob;

    @WrapMethod(method = "setLookAt(DDDFF)V")
    public void wrapLookAt(double x, double y, double z, float deltaYaw, float deltaPitch, Operation<Void> original) {
        DimensionTransformer transformer = mob.level().getTransformer().SSO();
        double unwrappedX = transformer.Coord.X.unwrap(mob.getX(), x);
        double unwrappedZ = transformer.Coord.Z.unwrap(mob.getZ(), z);

        if (unwrappedX != x) {
            x = mob.getX() + transformer.Coord.X.deltaFromBounds(mob.getX(), x);
        }

        if (unwrappedZ != z) {
            z = mob.getZ() + transformer.Coord.Z.deltaFromBounds(mob.getZ(), z);
        }

        original.call(x, y, z, deltaYaw, deltaPitch);
    }
}
