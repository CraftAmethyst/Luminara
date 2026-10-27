package org.craftamethyst.luminara.common.mod.util.log;

import org.apache.logging.log4j.Logger;

/** Shared logger that does not depend on a loader-specific entry class. */
public final class LuminaraLog {

  public static final Logger LOGGER = ArclightI18nLogger.getLogger("Luminara");

  private LuminaraLog() {}
}
