package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public final class ScaleParameter extends IntParameter {

  public static final int AUTO = -1;
  public static final int FIXEDRATIO = -2;

  private static final int MAX_SCALE = 1000;

  public ScaleParameter(String name, Params params, String desc,
                        int defValue) {
    super(name, params, true, false, desc, defValue, FIXEDRATIO, MAX_SCALE);
  }

  public static int parse(String scaleString) {
    if (scaleString.toLowerCase().startsWith("a"))
      return AUTO;
    else if (scaleString.toLowerCase().startsWith("f"))
      return FIXEDRATIO;
    else {
      scaleString = scaleString.replaceAll("[^\\d]", "");
      int sf = -1;
      try {
        sf = Integer.parseInt(scaleString);
      } catch (NumberFormatException e) {}
      if (sf >= 1 && sf <= MAX_SCALE) {
        return sf;
      }
    }
    return 0;
  }

  public synchronized boolean set(String scaleString) {
    int sf = parse(scaleString);
    if (sf == 0)
      throw new ErrorException(getName() + " parameter is incorrect");
    return set(sf);
  }

  public boolean setDefault(String scaleString) {
    int sf = parse(scaleString);
    if (sf == 0)
      return false;
    return super.setDefault(sf);
  }

  private String getStr(int value) {
    if (value == AUTO)
      return "Auto";
    else if (value == FIXEDRATIO)
      return "FixedRatio";
    else
      return Integer.toString(value);
  }

  public synchronized String getDefaultStr() { return getStr(defValue); }
  public synchronized String getStr() { return getStr(value); }

  public String getValues() {
    return "1-" + maxValue + ", Auto, or FixedRatio";
  }

  public int getReverseScaled(int dimension) {
    if (value == 100 || value == AUTO || value == FIXEDRATIO)
      return dimension;

    int reverseScaledFloor =
      (int)Math.floor((float)dimension * 100.0 / (float)value);
    int reverseScaledCeil =
      (int)Math.ceil((float)dimension * 100.0 / (float)value);

    if ((int)Math.floor((float)reverseScaledCeil * (float)value / 100.0) >
        dimension)
      return reverseScaledFloor;
    return reverseScaledCeil;
  }
};
