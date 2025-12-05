package com.turbovnc.rfb;

public class StringParameter extends VoidParameter {

  public StringParameter(String name, Params params, boolean isGUI,
                         boolean advanced, String desc, String defValue_) {
    this(name, params, isGUI, advanced, desc, defValue_, null);
  }

  public StringParameter(String name, Params params, boolean isGUI,
                         boolean advanced, String desc, String defValue_,
                         String values_) {
    super(name, params, isGUI, advanced, desc);
    value = defValue = defValue_;
    values = values_;
  }

  public synchronized boolean set(String str) {
    if (str != null && str.isEmpty())
      str = null;
    value = str;
    isDefault = false;
    setCommandLine(false);
    return value != null;
  }

  public final synchronized void reset() {
    value = defValue;
    isDefault = true;
    setCommandLine(false);
  }

  public synchronized boolean setDefault(String str) {
    if (str != null && str.isEmpty())
      str = null;
    value = defValue = str;
    isDefault = true;
    return true;
  }

  public final synchronized void setDefault(boolean isDefault_) {
    isDefault = isDefault_;
  }

  public final synchronized String get() { return value; }
  public final synchronized String getDefaultStr() { return defValue; }
  public final synchronized String getStr() { return value; }
  public final String getValues() { return values; }
  public final synchronized boolean isDefault() { return isDefault; }

  private boolean isDefault = true;

  String value;
  private String defValue;
  private final String values;
}
