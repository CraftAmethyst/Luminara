package org.craftamethyst.luminara.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class LoggingSpec {

  @Setting("use-simple-format")
  private boolean useSimpleFormat = false;

  public boolean isUseSimpleFormat() {
    return useSimpleFormat;
  }
}
