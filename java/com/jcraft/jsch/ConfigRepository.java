package com.jcraft.jsch;

public interface ConfigRepository {

  public Config getConfig(String host);

  public interface Config {
    public String getHostname();

    public String getUser();

    public int getPort();

    public String getValue(String key);

    public String[] getValues(String key);
  }

  static final Config defaultConfig = new Config() {
    @Override
    public String getHostname() {
      return null;
    }

    @Override
    public String getUser() {
      return null;
    }

    @Override
    public int getPort() {
      return -1;
    }

    @Override
    public String getValue(String key) {
      return null;
    }

    @Override
    public String[] getValues(String key) {
      return null;
    }
  };

  static final ConfigRepository nullConfig = new ConfigRepository() {
    @Override
    public Config getConfig(String host) {
      return defaultConfig;
    }
  };
}
