package io.izzel.arclight.fabric.mod;

import io.izzel.arclight.common.mod.ArclightCommon;
import io.izzel.arclight.common.mod.ArclightMixinPlugin;
import io.izzel.arclight.mixin.MixinTools;

public class FabricMixinPlugin extends ArclightMixinPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        ArclightCommon.setInstance(new FabricCommonImpl());
        MixinTools.setup();
        super.onLoad(mixinPackage);
    }
}
