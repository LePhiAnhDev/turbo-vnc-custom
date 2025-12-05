package com.turbovnc.rfb;

public final class ServerNameParameter extends StringParameter {

  public ServerNameParameter(String name, Params params, boolean isGUI,
                             boolean advanced, String desc, String defValue) {
    super(name, params, isGUI, advanced, desc, defValue);
    setDefault(defValue);
  }

  public synchronized boolean set(String str) {
    if (str != null && !str.isEmpty())
      str = str.replaceAll("\\s", "");
    return super.set(str);
  }

  public synchronized boolean setDefault(String str) {
    if (str != null && !str.isEmpty())
      str = str.replaceAll("\\s", "");
    return super.setDefault(str);
  }
}
