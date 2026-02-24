package io.papermc.paper.math;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface Position {

    static FinePosition fine(final double x, final double y, final double z) {
        return new FinePositionImpl(x, y, z);
    }

    static BlockPosition block(final int x, final int y, final int z) {
        return new BlockPositionImpl(x, y, z);
    }

    double x();

    double y();

    double z();

    default int blockX() {
        return (int) Math.floor(this.x());
    }

    default int blockY() {
        return (int) Math.floor(this.y());
    }

    default int blockZ() {
        return (int) Math.floor(this.z());
    }

    record FinePositionImpl(double x, double y, double z) implements FinePosition {
    }

    record BlockPositionImpl(int blockX, int blockY, int blockZ) implements BlockPosition {
        @Override
        public double x() {
            return this.blockX;
        }

        @Override
        public double y() {
            return this.blockY;
        }

        @Override
        public double z() {
            return this.blockZ;
        }
    }
}
