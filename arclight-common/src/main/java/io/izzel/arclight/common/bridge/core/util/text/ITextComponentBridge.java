package io.izzel.arclight.common.bridge.core.util.text;

import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.stream.Stream;

public interface ITextComponentBridge {
    Stream<Component> bridge$stream();

    Iterator<Component> bridge$iterator();
}
