package com.fexl.circumnavigate.mixin.packet;

import com.fexl.circumnavigate.processing.PacketTransformer;
import com.fexl.circumnavigate.storage.TransformerRequests;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Unique
    Connection thiz = (Connection) (Object) this;

    /**
     * Processes incoming packets from a client.
     */
    @WrapMethod(method = "genericsFtw")
    private static void packetReceive(Packet<?> packet, PacketListener listener, Operation<Void> original) {
        if(listener instanceof ServerCommonPacketListenerImpl commonPacketListener) {
            ServerPlayer player = getPlayerByConnection(commonPacketListener.connection);
            if(player == null) {
                original.call(packet, listener);
                return;
            }

            original.call(PacketTransformer.process(packet, player), listener);
        }
        else original.call(packet, listener);
    }

    /**
     * Processes outgoing packets to a client.
     */
    @WrapMethod(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V")
    public void packetSend(Packet<?> packet, @Nullable PacketSendListener listener, boolean flush, Operation<Void> original) {
        ServerPlayer player = getPlayerByConnection(thiz);
        if(player == null) {
            original.call(packet, listener, flush);
            return;
        }

        original.call(PacketTransformer.process(packet, player), listener, flush);
    }

    @Unique
    private static ServerPlayer getPlayerByConnection(Connection connection) {
        for(ServerPlayer player : TransformerRequests.server.getPlayerList().getPlayers()) {
            if(player.connection.connection.equals(connection) ) {
                return player;
            }
        }
        return null;
    }
}
