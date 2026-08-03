package io.izzel.arclight.common.mod.util.remapper;

import io.izzel.arclight.api.PluginPatcher;
import io.izzel.arclight.common.mod.util.remapper.patcher.ArclightPluginPatcher;
import io.izzel.arclight.common.mod.util.remapper.patcher.PluginLoggerTransformer;

import java.util.ArrayList;
import java.util.List;

final class RemapperPipeline {

    private RemapperPipeline() {
    }

    static List<PluginTransformer> create(boolean useJulBridge, List<PluginPatcher> patchers) {
        List<PluginTransformer> transformers = new ArrayList<>();
        transformers.add(CraftBukkitVersionRemapper.INSTANCE);
        transformers.add(ArclightInterfaceInvokerGen.INSTANCE);
        transformers.add(ArclightRedirectAdapter.INSTANCE);
        transformers.add(ClassLoaderAdapter.INSTANCE);
        if (!useJulBridge) {
            transformers.add(new PluginLoggerTransformer());
        }
        transformers.add(new ArclightPluginPatcher(patchers));
        return List.copyOf(transformers);
    }
}
