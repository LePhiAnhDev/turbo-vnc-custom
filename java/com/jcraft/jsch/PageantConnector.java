package com.jcraft.jsch;

public class PageantConnector implements AgentConnector {

  public PageantConnector() throws AgentProxyException {
  }

  public String getName() {
    return "pageant";
  }

  public static boolean isConnectorAvailable() {
    return System.getProperty("os.name").startsWith("Windows");
  }

  public boolean isAvailable() {
    return isConnectorAvailable();
  }

  public void query(Buffer buffer) throws AgentProxyException {
    if (!com.turbovnc.rfb.Helper.isAvailable())
      return;
    queryPageant(buffer);
  }

  private native void queryPageant(Buffer buffer);
}
