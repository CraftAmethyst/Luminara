package io.izzel.luminara.smoke;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

@Mod("luminara_smoke")
public final class SmokeMod {

    public SmokeMod() {
        LogManager.getLogger(SmokeMod.class).info("LUMINARA_SMOKE_MOD_ENABLED");
    }
}
