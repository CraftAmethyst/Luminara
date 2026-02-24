package io.papermc.paper.entity;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public sealed interface TeleportFlag permits TeleportFlag.EntityState, TeleportFlag.Relative {

    @ApiStatus.Experimental
    enum Relative implements TeleportFlag {
        X,
        Y,
        Z,
        YAW,
        PITCH
    }

    @ApiStatus.Experimental
    enum EntityState implements TeleportFlag {
        RETAIN_PASSENGERS,
        RETAIN_VEHICLE,
        RETAIN_OPEN_INVENTORY
    }
}
