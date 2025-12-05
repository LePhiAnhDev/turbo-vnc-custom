package com.turbovnc.rfb;

public class IntParameter extends VoidParameter {

  public IntParameter(String name, Params params, boolean isGUI,
                      boolean advanced, String desc, int defValue_) {
    super(name, params, isGUI, advanced, desc);
    value = defValue = defValue_;
    minValue = Integer.MIN_VALUE;
    maxValue = Integer.MAX_VALUE;
    useMin = useMax = false;
  }

  public IntParameter(String name, Params params, boolean isGUI,
                      boolean advanced, String desc, int defValue_,
                      int minValue_) {
    super(name, params, isGUI, advanced, desc);
    value = defValue = defValue_;
    minValue = minValue_;
    maxValue = Integer.MAX_VALUE;
    useMin = true;
    useMax = false;
  }

  public IntParameter(String name, Params params, boolean isGUI,
                      boolean advanced, String desc, int defValue_,
                      int minValue_, int maxValue_) {
    super(name, params, isGUI, advanced, desc);
    value = defValue = defValue_;
    minValue = minValue_;
    maxValue = maxValue_;
    useMin = useMax = true;
  }

  public boolean set(String str) {
    int i;
    try {
      i = Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return false;
    }
    return set(i);
  }

  public final synchronized boolean set(int value_) {
    if ((useMin && value_ < minValue) || (useMax && value_ > maxValue))
      return false;
    value = value_;
    isDefault = false;
    setCommandLine(false);
    return true;
  }

  public final synchronized void reset() {
    set(defValue);
    isDefault = true;
  }

  public final synchronized boolean setDefault(int defValue_) {
    if ((useMin && defValue_ < minValue) || (useMax && defValue_ > maxValue))
      return false;
    value = defValue = defValue_;
    isDefault = true;
    return true;
  }

  public boolean setDefault(String str) {
    int i;
    try {
      i = Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return false;
    }
    return setDefault(i);
  }

  public synchronized int get() { return value; }

  public synchronized String getDefaultStr() {
    if (defValue >= 0)
      return Integer.toString(defValue);
    return null;
  }

  public synchronized int getDefault() {
    return defValue;
  }

  public synchronized String getStr() { return Integer.toString(value); }

  public String getValues() {
    if (useMin || useMax) {
      return (useMin ? minValue : "") + "-" + (useMax ? maxValue : "");
    }
    return null;
  }

  public final synchronized void setDefault(boolean isDefault_) {
    isDefault = isDefault_;
  }

  public final synchronized boolean isDefault() { return isDefault; }

  private boolean isDefault = true;

  int value, defValue;
  final int minValue, maxValue;
  private final boolean useMin, useMax;
}
