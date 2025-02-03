package com.fexl.circumnavigate.mixin.client;

import com.fexl.circumnavigate.CircumnavigateClient;
import com.fexl.circumnavigate.core.DimensionTransformer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Add "DimensionBounds" uniform so it can be accessed by the curvature shader.
 */
@Debug(export = true)
@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {

	@Shadow public abstract Uniform getUniform(String name);

	@Unique
	public Uniform CURVATURE_WIDTH;

	/**
     * Initialize the "DimensionBounds" uniform.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void initCurveWidth(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat, CallbackInfo ci) {
        this.CURVATURE_WIDTH = this.getUniform("DimensionBounds");
    }

    /**
     * Reinitialize the "DimensionBounds" uniform. Used when turning on/off shaders or changing dimensions.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Inject(method = "setDefaultUniforms", at = @At("HEAD"))
    public void setCurveWidth(VertexFormat.Mode mode, Matrix4f projectionMatrix, Matrix4f frustrumMatrix, Window window, CallbackInfo ci) {
        if (CURVATURE_WIDTH != null) {;
	        DimensionTransformer transformer = Minecraft.getInstance().level.getTransformer();

			//Deactivate if settings is false
			if(CircumnavigateClient.USE_INTERNAL_CURVATURE_SHADER)
				CURVATURE_WIDTH.set(transformer.xWidth*16, transformer.zWidth*16);
			else
				CURVATURE_WIDTH.set(DimensionTransformer.DISABLED.xWidth, DimensionTransformer.DISABLED.zWidth);
        }

    }
}
