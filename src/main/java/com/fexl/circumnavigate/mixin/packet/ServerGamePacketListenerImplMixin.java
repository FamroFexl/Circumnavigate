/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.packet;

import com.fexl.circumnavigate.core.DimensionTransformer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@WrapOperation(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", ordinal = 0), require = 1)
	private Vec3 unwrapVec(Vec3 instance, Vec3 vec, Operation<Vec3> original) {
		DimensionTransformer transformer = player.serverLevel().getTransformer();
		return original.call(instance, transformer.Vector3D.unwrap(instance, vec));
	}

	@WrapOperation(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;II)V"), require = 1)
	private void wrapBlockPos(ServerPlayerGameMode instance, BlockPos pos, net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action action, net.minecraft.core.Direction direction, int worldHeight, int sequence, Operation<Void> original) {
		original.call(instance, player.serverLevel().getTransformer().Block.wrap(pos), action, direction, worldHeight, sequence);
	}

	@WrapOperation(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundUseItemOnPacket;getHitResult()Lnet/minecraft/world/phys/BlockHitResult;"), require = 1)
	private BlockHitResult wrapLocationAndBlockPos(ServerboundUseItemOnPacket instance, Operation<BlockHitResult> original) {
		DimensionTransformer transformer = player.serverLevel().getTransformer();
		BlockHitResult blockHit = original.call(instance);

		return new BlockHitResult(transformer.Vector3D.wrap(blockHit.getLocation()), blockHit.getDirection(), transformer.Block.wrap(blockHit.getBlockPos()), blockHit.isInside());
	}

	/**
	 * Vanilla computes the wrapped X delta three times in handleMovePlayer:
	 * first-good, last-good, and post-move residual. Normalize each store to the shortest wrapped
	 * delta so every later check in the method sees the same seam-aware X movement.
	 */
	@ModifyVariable(method = "handleMovePlayer", at = @At("STORE"), index = 17, require = 3, expect = 3)
	private double normalizePlayerMoveDeltaX(double deltaX) {
		return normalizeWrappedDelta(deltaX, player.serverLevel().getTransformer().Coord.X.domainLength);
	}

	/**
	 * Same as {@link #normalizePlayerMoveDeltaX(double)} for Z.
	 */
	@ModifyVariable(method = "handleMovePlayer", at = @At("STORE"), index = 21, require = 3, expect = 3)
	private double normalizePlayerMoveDeltaZ(double deltaZ) {
		return normalizeWrappedDelta(deltaZ, player.serverLevel().getTransformer().Coord.Z.domainLength);
	}

	/**
	 * Vehicle movement has the same wrapped-delta problem for the root vehicle. Normalize each X store
	 * so both the fast-move and moved-wrongly checks see the shortest seam-crossing delta.
	 */
	@ModifyVariable(method = "handleMoveVehicle", at = @At("STORE"), index = 18, require = 3, expect = 3)
	private double normalizeVehicleMoveDeltaX(double deltaX) {
		return normalizeWrappedDelta(deltaX, player.serverLevel().getTransformer().Coord.X.domainLength);
	}

	/**
	 * Same as {@link #normalizeVehicleMoveDeltaX(double)} for Z.
	 */
	@ModifyVariable(method = "handleMoveVehicle", at = @At("STORE"), index = 22, require = 3, expect = 3)
	private double normalizeVehicleMoveDeltaZ(double deltaZ) {
		return normalizeWrappedDelta(deltaZ, player.serverLevel().getTransformer().Coord.Z.domainLength);
	}

	/**
	 * Raw wrapped deltas jump by one full domain when movement crosses a seam. Convert them back to
	 * the shortest signed delta, which is what vanilla would have seen in an unwrapped world.
	 */
	@Unique
	private static double normalizeWrappedDelta(double delta, int domainLength) {
		double domainRadius = domainLength / 2.0;
		if (delta > domainRadius) {
			return delta - domainLength;
		}
		if (delta < -domainRadius) {
			return delta + domainLength;
		}
		return delta;
	}
}
