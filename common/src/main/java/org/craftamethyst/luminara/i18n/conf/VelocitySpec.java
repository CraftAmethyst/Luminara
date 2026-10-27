package org.craftamethyst.luminara.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class VelocitySpec {

  @Setting("enabled")
  private boolean enabled;

  @Setting("online-mode")
  private boolean onlineMode;

  @Setting("forwarding-secret")
  private String forwardingSecret;

  @Setting("debug-logging")
  private boolean debugLogging;

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isOnlineMode() {
    return onlineMode;
  }

  public String getForwardingSecret() {
    return forwardingSecret != null ? forwardingSecret : "";
  }

  public boolean isDebugLogging() {
    return debugLogging;
  }
}
