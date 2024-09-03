package com.fexl.circumnavigate.injected;

import com.fexl.circumnavigate.util.WorldTransformer;

public interface LevelTransformer {
	default WorldTransformer getTransformer() {
		return null;
	}

	default void setTransformer(WorldTransformer transformer) {

	}
}
