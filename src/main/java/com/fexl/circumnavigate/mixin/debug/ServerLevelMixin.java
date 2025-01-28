package com.fexl.circumnavigate.mixin.debug;

import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	//Send structure starts on init, not just on generation
	@Inject(method = "onStructureStartsAvailable", at = @At("HEAD"))
	public void sendStructurePacket(ChunkAccess chunk, CallbackInfo ci) {
		chunk.getAllStarts().forEach((structure, start) -> {
			DebugPackets.sendStructurePacket((WorldGenLevel) this, start);
		});
	}
}
