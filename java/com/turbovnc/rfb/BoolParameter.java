package com.turbovnc.rfb;

public final class BoolParameter extends VoidParameter {

  public BoolParameter(String name, Params params, boolean isGUI,
                       boolean advanced, String desc, boolean defValue_) {
    super(name, params, isGUI, advanced, desc);
    value = defValue = defValue_;
  }

  public boolean set(String str) {
    return set(str, false, false);
  }

  public synchronized boolean set(String str, boolean reverse_,
                                  boolean commandLine_) {
    if (str.equals("1") || str.equalsIgnoreCase("on") ||
        str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes"))
      set(reverse_ ? false : true);
    else if (str.equals("0") || str.equalsIgnoreCase("off") ||
             str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no"))
      set(reverse_ ? true : false);
    else
      return false;
    setCommandLine(commandLine_);
    return true;
  }

  public synchronized void set(boolean value_) {
    value = value_;
    setCommandLine(false);
  }

  public synchronized void reset() {
    set(defValue);
    reverse = false;
  }

  public synchronized boolean setDefault(String str) {
    if (str.equals("1") || str.equalsIgnoreCase("on") ||
        str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes"))
      value = defValue = true;
    else if (str.equals("0") || str.equalsIgnoreCase("off") ||
             str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no"))
      value = defValue = false;
    else
      return false;
    return true;
  }

  public synchronized boolean get() { return value; }
  public synchronized String getDefaultStr() { return defValue ? "1" : "0"; }
  public synchronized String getStr() { return Boolean.toString(value); }
  public String getValues() { return "0, 1"; }
  public boolean isBool() { return true; }

  @SuppressWarnings("checkstyle:VisibilityModifier")
  public boolean reverse;

  private boolean value, defValue;
}
