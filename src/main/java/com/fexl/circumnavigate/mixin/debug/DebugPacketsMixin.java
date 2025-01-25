/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugPackets.class)
public abstract class DebugPacketsMixin {
	@Shadow private static void sendPacketToAllPlayers(ServerLevel level, CustomPacketPayload payload) {};

	@Inject(method = "sendNeighborsUpdatePacket", at = @At("HEAD"))
	private static void sendNeighborsUpdatePacket(Level level, BlockPos pos, CallbackInfo ci) {
		sendPacketToAllPlayers((ServerLevel) level, new NeighborUpdatesDebugPayload(level.getGameTime(), pos));
	}

	@Inject(method = "sendPathFindingPacket", at = @At("HEAD"))
	private static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint, CallbackInfo ci) {
		if(path != null) sendPacketToAllPlayers((ServerLevel) level, new PathfindingDebugPayload(mob.getId(), path, maxDistanceToWaypoint));
	}

	@Inject(method = "sendStructurePacket", at = @At("HEAD"))
	private static void sendStructurePacket(WorldGenLevel level, StructureStart structureStart, CallbackInfo ci) {
		List<StructuresDebugPayload.PieceInfo> pieces = new ArrayList<>();

		structureStart.getPieces().forEach(structurePiece -> pieces.add(new StructuresDebugPayload.PieceInfo(structurePiece.getBoundingBox(), pieces.isEmpty())));

		sendPacketToAllPlayers(level.getLevel(), new StructuresDebugPayload(level.getLevel().dimension(), structureStart.getBoundingBox(), pieces));
	}
}
