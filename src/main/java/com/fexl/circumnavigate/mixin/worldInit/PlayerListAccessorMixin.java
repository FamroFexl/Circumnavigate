/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.worldInit;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerList.class)
public interface PlayerListAccessorMixin {
	@Accessor List<ServerPlayer> getPlayers();
	@Accessor int getViewDistance();
	@Accessor void setViewDistance(int viewDistance);

	@Accessor MinecraftServer getServer();
}
