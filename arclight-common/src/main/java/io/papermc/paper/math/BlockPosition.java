package io.papermc.paper.math;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface BlockPosition extends Position {

    @Override
    int blockX();

    @Override
    int blockY();

    @Override
    int blockZ();
}
