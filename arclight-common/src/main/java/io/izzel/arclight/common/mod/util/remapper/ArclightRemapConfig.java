package io.izzel.arclight.common.mod.util.remapper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/*
 * Used to record transformation detail for specific ClassLoaders.
 */
public record ArclightRemapConfig(boolean remap) {
    public static final ArclightRemapConfig PLUGIN = new ArclightRemapConfig(
        true
    );

    public static ArclightRemapConfig read(DataInput input) throws IOException {
        return new ArclightRemapConfig(input.readBoolean());
    }

    public ArclightRemapConfig copy() {
        return new ArclightRemapConfig(remap);
    }

    public int write(DataOutput output) throws IOException {
        output.writeBoolean(remap);
        return 1;
    }
}
