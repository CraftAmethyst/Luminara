package io.izzel.arclight.common.mod;

import com.google.common.graph.Graph;
import java.util.Set;

public class ArclightCommon {

  public interface Api {

    byte[] platformRemapClass(byte[] cl);

    boolean isModLoaded(String modid);

    /**
     * This is here because Fabric and NeoForge use different version of Guava. The signature for
     * this method changed between these versions, leading to NoSuchMethodError. It is recommended
     * to check this method and remove it if necessary when upgrading.
     */
    <T> Set<T> guavaReachableNodes(Graph<T> graph, T node);
  }

  private static Api instance;

  public static Api api() {
    return instance;
  }

  public static void setInstance(Api instance) {
    ArclightCommon.instance = instance;
  }
}
