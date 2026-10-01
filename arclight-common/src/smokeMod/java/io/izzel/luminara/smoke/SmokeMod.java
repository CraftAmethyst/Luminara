package io.izzel.luminara.smoke;

import net.minecraftforge.fml.common.Mod;

@Mod("luminara_smoke")
public final class SmokeMod {

    public SmokeMod() {
        // Printed rather than logged so the fixture needs no Forge artifact to compile.
        // The smoke task asserts on this exact marker in the server log.
        System.out.println("LUMINARA_SMOKE_MOD_ENABLED");
    }
}
