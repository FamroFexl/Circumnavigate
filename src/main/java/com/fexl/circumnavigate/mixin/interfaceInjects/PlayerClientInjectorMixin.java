/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.interfaceInjects;

import com.fexl.circumnavigate.injected.ServerPlayerInjector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class PlayerClientInjectorMixin implements ServerPlayerInjector {
	private double clientX = ((ServerPlayer)(Object)this).getX();
	private double clientZ = ((ServerPlayer)(Object)this).getZ();

	@Override
	public double getClientX() { return clientX; }

	@Override
	public double getClientZ() {
		return clientZ;
	}

	@Override
	public ChunkPos getClientChunk() { return new ChunkPos((int) (clientX / 16), (int) (clientZ / 16)); }

	@Override
	public BlockPos getClientBlock() { return new BlockPos(Mth.floor(clientX), ((ServerPlayer)(Object)this).blockPosition().getY(), Mth.floor(clientZ)); }

	@Override
	public Vec3 getClientPosition() { return new Vec3(clientX, ((ServerPlayer)(Object)this).position().y, clientZ); }

	@Override
	public void setClientX(double clientX) {
		this.clientX = clientX;
	}

	@Override
	public void setClientZ(double clientZ) {
		this.clientZ = clientZ;
	}

}
