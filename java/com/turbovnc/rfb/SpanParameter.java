package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public final class SpanParameter extends IntParameter {

  public static final int PRIMARY = 0;
  public static final int ALL = 1;
  public static final int AUTO = 2;

  public SpanParameter(String name, Params params, String desc, int defValue) {
    super(name, params, true, false, desc, defValue, PRIMARY, AUTO);
  }

  public synchronized boolean set(String spanString) {
    if (spanString.toLowerCase().startsWith("p"))
      return set(PRIMARY);
    else if (spanString.toLowerCase().startsWith("al"))
      return set(ALL);
    else if (spanString.toLowerCase().startsWith("au"))
      return set(AUTO);
    else
      throw new ErrorException(getName() + " parameter is incorrect");
  }

  public boolean setDefault(String spanString) {
    if (spanString.toLowerCase().startsWith("p"))
      return super.setDefault(PRIMARY);
    else if (spanString.toLowerCase().startsWith("al"))
      return super.setDefault(ALL);
    else if (spanString.toLowerCase().startsWith("au"))
      return super.setDefault(AUTO);
    else
      return false;
  }

  private String getStr(int value) {
    if (value == PRIMARY)
      return "Primary";
    else if (value == ALL)
      return "All";
    else if (value == AUTO)
      return "Auto";
    else
      return null;
  }

  public synchronized String getDefaultStr() { return getStr(defValue); }
  public synchronized String getStr() { return getStr(value); }

  public String getValues() { return "Primary, All, Auto"; }
};
