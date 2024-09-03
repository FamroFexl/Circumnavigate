/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * For testing only
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerToClientListener {
	private static final Logger LOGGER = LogUtils.getLogger();

	String previousLine;
	String currentLine;
	int lineCount = 1;

	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
	public void send(Packet<?> packet, CallbackInfo ci) {
		//if(isGamePacket(packet.getClass()))
		Class<?> clazz = packet.getClass();
		if(true)
			return;

		//if(!isInExceptions(clazz.getSimpleName()))
			//return;
		String log = "S->C: ";

		if(clazz.getEnclosingClass() != null)
			log += clazz.getEnclosingClass().getSimpleName() + "." + clazz.getSimpleName();
		else
			log += clazz.getSimpleName();
		log += " ";

		if(packet instanceof ClientboundBlockUpdatePacket) {
			ClientboundBlockUpdatePacket blockUpdate = (ClientboundBlockUpdatePacket) packet;
			log += blockUpdate.getPos() + " " + blockUpdate.getBlockState().getBlock();
		}

		if(packet instanceof ClientboundBlockChangedAckPacket) {
			ClientboundBlockChangedAckPacket blockAck = (ClientboundBlockChangedAckPacket) packet;
			log += blockAck.sequence();
		}

		currentLine = log;

		//If the current line equals the previous line
		if(currentLine.equals(previousLine)) {
			//Increment the line count
			lineCount++;
		}
		//Not equal to the previous line
		else {
			if(previousLine == null)
				return;
			//LOGGER.info(previousLine + ((lineCount != 1) ? (" {" + lineCount + "}") : ""));
			lineCount = 1;
			previousLine = currentLine;
		}


	}
	/**
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"))
	public void send(Packet<?> packet, @Nullable PacketSendListener listener, CallbackInfo ci) {
		//if(isGamePacket(packet.getClass()))
			System.out.println("S->C (2): " + packet.getClass().getSimpleName());
	}**/

	public boolean isGamePacket(Class<?> clazz) {
		Type[] interfaces = clazz.getGenericInterfaces();
		for(Type iface : interfaces) {
			if (iface instanceof ParameterizedType) {
				ParameterizedType paramType = (ParameterizedType) iface;
				if (paramType.getRawType().equals(Packet.class)) {
					Type[] typeArgs = paramType.getActualTypeArguments();
					if (typeArgs.length == 1 && typeArgs[0].equals(ServerGamePacketListener.class)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isntInExceptions(String name) {
		List<String> stringList = Arrays.asList("ClientboundSetEntityMotionPacket", "ClientboundTeleportEntityPacket", "ClientboundMoveEntityPacket", "Pos", "Rot", "PosRot", "ClientboundRotateHeadPacket");
		return stringList.contains(name);
	}

	public boolean isInExceptions(String name) {
		List<String> stringList = Arrays.asList("ClientboundBlockUpdatePacket", "ClientboundBlockChangedAckPacket");
		return stringList.contains(name);
	}
}
