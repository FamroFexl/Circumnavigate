package com.fexl.circumnavigate.mixin.client.chunkHandle;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Options.class)
public interface OptionsAccessorMixin {
	@Accessor int getServerRenderDistance();
	@Accessor OptionInstance<Integer> getRenderDistance();
}
