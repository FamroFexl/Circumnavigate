/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.options.WorldWrappingSettings;
import com.fexl.circumnavigate.storage.WrappingDataStorage;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@SuppressWarnings("deprecation")
@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
	/**
	 * Inject wrappingSettings into the level.dat.
	 */
	@Inject(method = "setTagData(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void injectSaveData(RegistryAccess registry, CompoundTag nbt, CompoundTag playerNBT, CallbackInfo ci) {
		if(WrappingDataStorage.settings != null) {
			nbt.put("WrappingSettings", WorldWrappingSettings.CODEC.encodeStart(NbtOps.INSTANCE, WrappingDataStorage.settings).getOrThrow());
		}
	}

	/**
	 * Retrieve the wrappingSettings save data from the level.dat on world selection.
	 */
	@Inject(method = "parse", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void parseInject(Dynamic<?> tag, LevelSettings levelSettings, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, WorldOptions worldOptions, Lifecycle worldGenSettingsLifecycle, CallbackInfoReturnable<PrimaryLevelData> cir) {
		Optional<WorldWrappingSettings> worldWrappingSettings = WorldWrappingSettings.CODEC.parse(tag.get("WrappingSettings").orElseEmptyMap()).result();
		WrappingDataStorage.settings = worldWrappingSettings.orElse(null);
	}
}
