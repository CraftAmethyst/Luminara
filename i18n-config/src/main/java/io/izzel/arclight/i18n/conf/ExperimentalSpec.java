package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ExperimentalSpec {

  @Setting("replaceable-base-world-generator")
  private boolean canOverrideWorldgen = true;

  public boolean canOverrideWorldgen() {
    return this.canOverrideWorldgen;
  }
}
