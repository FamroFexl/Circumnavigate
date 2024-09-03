/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.options.WrappingSettings;
import com.fexl.circumnavigate.util.ServerTransformer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("deprecation")
@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
	/**
	 * Inject wrappingSettings into the level.dat.
	 */
	@Inject(method = "setTagData(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void injectSaveData(RegistryAccess registry, CompoundTag nbt, CompoundTag playerNBT, CallbackInfo ci) {
		nbt = WrappingSettings.serialize(nbt);
	}

	/**
	 * Retrieve the wrappingSettings save data on world creation.
	 */
	@Inject(method = "<init>(Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/storage/PrimaryLevelData$SpecialWorldProperty;Lcom/mojang/serialization/Lifecycle;)V", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
	}

	/**
	 * Retrieve the wrappingSettings save data from the level.dat on world selection.
	 */
	@Inject(method = "parse", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void parseInject(Dynamic<?> tag, LevelSettings levelSettings, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, WorldOptions worldOptions, Lifecycle worldGenSettingsLifecycle, CallbackInfoReturnable<PrimaryLevelData> cir) {
		WrappingSettings settings = WrappingSettings.parse(tag);
		if(settings.equals(WrappingSettings.CORRUPT)) {
			//TODO: Cancel world load. Wrapping settings are corrupt and cannot be used.
		}
	}
}
