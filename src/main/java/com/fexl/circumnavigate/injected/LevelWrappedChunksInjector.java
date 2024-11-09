package com.fexl.circumnavigate.injected;

import com.fexl.circumnavigate.core.WrappedChunks;

public interface LevelWrappedChunksInjector {
    default WrappedChunks getWrappedChunks() {
        return null;
    }

    default void setWrappedChunks(WrappedChunks wrappedChunks) {

    }
}
