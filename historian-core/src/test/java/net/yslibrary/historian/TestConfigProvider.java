package net.yslibrary.historian;

import com.google.auto.service.AutoService;

import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.config.GlobalConfigProvider;

@AutoService(GlobalConfigProvider.class)
public class TestConfigProvider implements GlobalConfigProvider {
  private static final int[] SDK = new int[]{23};

  @Override
  public Config get() {
    System.out.println("TestConfigProvider");
    return new Config.Builder()
        .setSdk(SDK)
        .setApplication(TestApp.class)
        .build();
  }
}
