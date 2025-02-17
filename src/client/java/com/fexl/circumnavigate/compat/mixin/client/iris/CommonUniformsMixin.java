package com.fexl.circumnavigate.compat.mixin.client.iris;

import com.fexl.circumnavigate.core.DimensionTransformer;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonUniforms.class)
public class CommonUniformsMixin {
    @Inject(method = "generalCommonUniforms", at = @At("HEAD"), remap = false)
    private static void general(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives, CallbackInfo ci) {
        uniforms.uniform2i(UniformUpdateFrequency.PER_FRAME, "dimensionBounds", CommonUniformsMixin::getDimensionBounds);
    }

    private static Vector2i getDimensionBounds() {
        DimensionTransformer transformer = Minecraft.getInstance().level.getTransformer();
        return new Vector2i(transformer.xWidth, transformer.zWidth);
    }
}
