package com.fexl.circumnavigate.accessors;

import net.minecraft.server.level.ServerLevel;

public interface LevelAccessor {
    public ServerLevel getLevel();

    public void setLevel(ServerLevel level);
}
