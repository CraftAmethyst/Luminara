package org.craftamethyst.luminara.i18n;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import net.minecraftforge.fml.config.IConfigSpec;

public final class ForgeLuminaraConfig extends UnmodifiableConfigWrapper<UnmodifiableConfig>
    implements IConfigSpec<ForgeLuminaraConfig> {

  private CommentedConfig loaded;
  private volatile boolean correcting;

  public ForgeLuminaraConfig() {
    super(ArclightConfig.defaults().unmodifiable());
  }

  @Override
  public void acceptConfig(CommentedConfig config) {
    loaded = config;
    if (config != null) {
      correct(config);
      afterReload();
    }
  }

  @Override
  public boolean isCorrecting() {
    return correcting;
  }

  @Override
  public boolean isCorrect(CommentedConfig config) {
    return !ArclightConfig.needsUpdate(config);
  }

  @Override
  public int correct(CommentedConfig config) {
    correcting = true;
    try {
      return ArclightConfig.prepare(config) ? 1 : 0;
    } finally {
      correcting = false;
    }
  }

  @Override
  public void afterReload() {
    ArclightConfig.useConfig(loaded);
  }
}
