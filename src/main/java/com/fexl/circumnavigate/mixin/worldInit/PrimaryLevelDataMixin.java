/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import com.fexl.circumnavigate.accessors.WorldWrappingSettingsAccessor;
import com.fexl.circumnavigate.options.WorldWrappingSettings;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@SuppressWarnings("deprecation")
@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin implements WorldWrappingSettingsAccessor {
	/**
	 * Inject wrappingSettings into the level.dat.
	 */
	@Inject(method = "setTagData(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "TAIL"))
	private void injectSaveData(RegistryAccess registry, CompoundTag nbt, CompoundTag playerNBT, CallbackInfo ci) {
		if(settings != null) {
			nbt.put("WrappingSettings", WorldWrappingSettings.CODEC.encodeStart(NbtOps.INSTANCE, settings).getOrThrow());
		}
	}

	/**
	 * Retrieve the wrappingSettings save data from the level.dat on world selection.
	 */
	@ModifyReturnValue(method = "parse", at = @At("RETURN"))
	private static PrimaryLevelData parse(PrimaryLevelData original, @Local(argsOnly = true) Dynamic<?> tag) {
		Optional<WorldWrappingSettings> worldWrappingSettings = WorldWrappingSettings.CODEC.parse(tag.get("WrappingSettings").orElseEmptyMap()).result();
		((WorldWrappingSettingsAccessor) (Object) original).setWorldWrappingSettings(worldWrappingSettings.orElse(null));

		return original;
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
