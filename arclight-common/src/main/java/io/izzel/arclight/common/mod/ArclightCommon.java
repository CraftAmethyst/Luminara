package io.izzel.arclight.common.mod;

public class ArclightCommon {

    private static Api instance;

    public static Api api() {
        return instance;
    }

    public static void setInstance(Api instance) {
        ArclightCommon.instance = instance;
    }

    public interface Api {

        default byte[] platformRemapClass(byte[] cl) {
            return cl;
        }

        boolean isModLoaded(String modid);
    }
}
