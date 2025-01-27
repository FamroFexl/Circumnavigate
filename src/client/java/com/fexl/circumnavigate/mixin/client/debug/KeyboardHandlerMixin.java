/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.client.debug;

import static com.fexl.circumnavigate.client.storage.DebugVariables.*;

import com.fexl.circumnavigate.Circumnavigate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Shadow protected abstract boolean handleChunkDebugKeys(int keyCode);
	@Shadow @Final private Minecraft minecraft;

	@Shadow protected abstract void debugFeedback(String message, Object... args);

	/**
	 * Bind discarded chunk debug keys to the F3 input event.
	 * <p>
	 * E: SectionPath
	 * L: SmartCull (this key disables tick metrics)
	 * U: Capture Frustrum (F3+Shift+U to disable)
	 * V: SectionVisibility
	 * W: WireFrame (does nothing)
	 */
	@Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
	public void handleDebugKeys(int key, CallbackInfoReturnable<Boolean> cir) {
		if(!Circumnavigate.DEV_MODE) return;

		if(handleChunkDebugKeys(key)) cir.setReturnValue(true);
	}

	/**
	 * Add an F6 input event for handling previously disabled debug renders.
	 */
	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	public void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
		if(!Circumnavigate.DEV_MODE) return;

		if (windowPointer == minecraft.getWindow().getWindow()) {
			boolean F6_DOWN = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F6);

			if(F6_DOWN && action != 0) {
				handleMoreDebugKeys(key);
				ci.cancel();
			}
		}
	}

	/**
	 * Add additional keys to handle disabled debug renders to an F6 input event.
	 * <p>
	 * N: NeighborUpdates
	 * S: StructurePieces
	 * P: PathFinding
	 * L: LightingLevels
	 * C: ChunkInfo
	 */
	@Unique
	private boolean handleMoreDebugKeys(int key) {
		switch(key) {
			case GLFW.GLFW_KEY_N:
				renderNeighborUpdates = !renderNeighborUpdates;
				this.debugFeedback("NeighborUpdates: {0}", renderNeighborUpdates ? "shown" : "hidden");
				break;
			case GLFW.GLFW_KEY_S:
				renderStructurePieces = !renderStructurePieces;
				this.debugFeedback("StructurePieces: {0}", renderStructurePieces ? "shown" : "hidden");
				break;
			case GLFW.GLFW_KEY_P:
				renderPathfinding = !renderPathfinding;
				this.debugFeedback("PathFinding: {0}", renderPathfinding ? "shown" : "hidden");
				break;
			case GLFW.GLFW_KEY_L:
				renderLightDebug = !renderLightDebug;
				this.debugFeedback("LightingLevels: {0}", renderLightDebug ? "shown" : "hidden");
				break;
			case GLFW.GLFW_KEY_C:
				renderChunkInfo = !renderChunkInfo;
				this.debugFeedback("ChunkInfo: {0}", renderChunkInfo ? "shown" : "hidden");
				break;
			default:
				return false;
		}
		return true;
	}
}
