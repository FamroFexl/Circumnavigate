package com.fexl.circumnavigate.mixin.client.shader;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Add "WorldBounds" uniform so it can be accessed by the curvature shader.
 */
@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {
    @Shadow public abstract Uniform getUniform(String name);

    @Unique @Nullable private Uniform CURVATURE_WIDTH;

    /**
     * Initialize the "WorldBounds" uniform.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void initCurveWidth(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat, CallbackInfo ci) {
        this.CURVATURE_WIDTH = this.getUniform("DimensionBounds");
    }

    /**
     * Reinitialize the "WorldBounds" uniform. Used when turning on/off shaders or changing dimensions.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Inject(method = "setDefaultUniforms", at = @At("HEAD"))
    public void setCurveWidth(VertexFormat.Mode mode, Matrix4f projectionMatrix, Matrix4f frustrumMatrix, Window window, CallbackInfo ci) {

        if (this.CURVATURE_WIDTH != null) {
            DimensionTransformer transformer = Minecraft.getInstance().level.getTransformer();
            this.CURVATURE_WIDTH.set(transformer.xWidth, transformer.zWidth);
        }
    }
}
