/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.packetHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {
	@ModifyArg(method = "sendChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkWithLightPacket;<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V"), index = 0)
	private static LevelChunk sendChunk(LevelChunk chunk, @Local ServerGamePacketListenerImpl packetListener) {
		WorldTransformer transformer = packetListener.player.level().getTransformer();
		ChunkPos unwrapped = transformer.translateChunkFromBounds(packetListener.player.getClientChunk(), chunk.getPos());
		LevelChunk send = new LevelChunk(chunk.level, unwrapped, chunk.getUpgradeData(), (LevelChunkTicks<Block>) chunk.getBlockTicks(), (LevelChunkTicks<Fluid>) chunk.getFluidTicks(), chunk.getInhabitedTime(), chunk.getSections(), chunk.postLoad, chunk.getBlendingData());
		return send;
	}
}
