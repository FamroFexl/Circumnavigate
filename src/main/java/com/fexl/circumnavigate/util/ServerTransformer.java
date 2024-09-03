/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.util;

/**
 * This may be required to separate instances in the case of an integrated server, i.e. same JVM. Otherwise, both client and server would both use the singleton instance of WorldTransformer.
 */
public class ServerTransformer extends WorldTransformer {
	static ServerTransformer instance = null;

	/**
	 * Initialized by custom info in a world's level.dat.
	 */
	public ServerTransformer(int xChunkBoundMin, int xChunkBoundMax, int zChunkBoundMin, int zChunkBoundMax, int xShift, int zShift) {
		super(xChunkBoundMin, xChunkBoundMax, zChunkBoundMin, zChunkBoundMax, xShift, zShift);
		instance = this;
	}


	public static ServerTransformer getInstance() {
		//instance should always be set before it is used, as it is only set on world load, and it is only used in world play
		return instance;
	}
}
