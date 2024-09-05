/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
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
@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientToServerListener {
	private static final Logger LOGGER = LogUtils.getLogger();

	String previousLine;
	String currentLine;
	int lineCount = 1;

	@Inject(method = "send", at = @At("HEAD"))
	public void send(Packet<?> packet, CallbackInfo ci) {
		//if(isGamePacket(packet.getClass()))
		Class<?> clazz = packet.getClass();
		//if(!isInExceptions(clazz.getSimpleName()))
			//return;
		if(true)
			return;
		String log = "C->S: ";
		if(clazz.getEnclosingClass() != null)
			log += clazz.getEnclosingClass().getSimpleName() + "." + clazz.getSimpleName();
		else
			log += clazz.getSimpleName();
		/**
		if(packet instanceof ServerboundMovePlayerPacket.Pos) {
			ServerboundMovePlayerPacket.Pos posUpdate = (ServerboundMovePlayerPacket.Pos) packet;
			log += " " + posUpdate.getX(-1) + ", " + posUpdate.getZ(-1);
		}**/

		currentLine = log;

		//If the current line equals the previous line
		if(currentLine.equals(previousLine)) {
			//Increment the line count
			lineCount++;
		}
		//Not equal to the previous line
		else {
			//LOGGER.info(previousLine + ((lineCount != 1) ? (" {" + lineCount + "}") : ""));
			lineCount = 1;
			previousLine = currentLine;
		}
	}

	public boolean isInExceptions(String name) {
		List<String> stringList = Arrays.asList("ServerboundPlayerActionPacket", "ServerboundSwingPacket");
		return stringList.contains(name);
	}

	public boolean isGamePacket(Class<?> clazz) {
		Type[] interfaces = clazz.getGenericInterfaces();
		for(Type iface : interfaces) {
			if (iface instanceof ParameterizedType) {
				ParameterizedType paramType = (ParameterizedType) iface;
				if (paramType.getRawType().equals(Packet.class)) {
					Type[] typeArgs = paramType.getActualTypeArguments();
					if (typeArgs.length == 1 && typeArgs[0].equals(ClientGamePacketListener.class)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
