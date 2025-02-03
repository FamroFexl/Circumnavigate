package com.fexl.circumnavigate.mixin.client.options;

import com.fexl.circumnavigate.CircumnavigateClient;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsScreenMixin {
	@Unique
	private static final OptionInstance<Boolean> useInternalCurvatureShader =  OptionInstance.createBoolean("options.use_internal_curvature_shader", OptionInstance.noTooltip(), true, boolean_ -> CircumnavigateClient.USE_INTERNAL_CURVATURE_SHADER = boolean_);

	/**
	 * Use internal curvature shader option in video settings.
	 */
	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] videoOptions(OptionInstance<?>[] original) {
		List<OptionInstance<?>> newArray = new ArrayList<>(Arrays.stream(original).toList());

		newArray.add(8, useInternalCurvatureShader);

		return newArray.toArray(new OptionInstance[0]);
	}
}
