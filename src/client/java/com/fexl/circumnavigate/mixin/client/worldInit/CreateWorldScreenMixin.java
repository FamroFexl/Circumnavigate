/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.client.worldInit;

import com.fexl.circumnavigate.accessors.WorldWrappingSettingsAccessor;
import com.fexl.circumnavigate.options.WorldWrappingSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin implements WorldWrappingSettingsAccessor {
	@WrapOperation(method = "createNewWorld", at = @At(value = "NEW", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;"))
	public PrimaryLevelData createNewWorld(LevelSettings settings, WorldOptions worldOptions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, Lifecycle worldGenSettingsLifecycle, Operation<PrimaryLevelData> original) {
		PrimaryLevelData newData = original.call(settings, worldOptions, specialWorldProperty, worldGenSettingsLifecycle);
		((WorldWrappingSettingsAccessor) (Object) newData).setWorldWrappingSettings(this.settings);

		return newData;
	}

	WorldWrappingSettings settings = null;

	@Override
	public void setWorldWrappingSettings(WorldWrappingSettings settings) {
		this.settings = settings;
	}

	@Override
	public WorldWrappingSettings getWorldWrappingSettings() {
		return settings;
	}
}
