package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public final class SubsampParameter extends IntParameter {

  public static final int NUMOPT = 4;

  public static final int NONE = 0;
  public static final int FOURX = 1;
  public static final int TWOX = 2;
  public static final int GRAY = 3;

  public SubsampParameter(String name, Params params, String desc,
                          int defValue) {
    super(name, params, true, false, desc, defValue, NONE, GRAY);
  }

  public synchronized boolean set(String subsampString) {
    switch (subsampString.toUpperCase().charAt(0)) {
      case '1':
      case 'N':
        return set(NONE);
      case '4':
        return set(FOURX);
      case '2':
        return set(TWOX);
      case 'G':
        return set(GRAY);
      default:
        throw new ErrorException(getName() + " parameter is incorrect");
    }
  }

  public boolean setDefault(String subsampString) {
    switch (subsampString.toUpperCase().charAt(0)) {
      case '1':
      case 'N':
        return super.setDefault(NONE);
      case '4':
        return super.setDefault(FOURX);
      case '2':
        return super.setDefault(TWOX);
      case 'G':
        return super.setDefault(GRAY);
      default:
        return false;
    }
  }

  public synchronized int getOrdinal() {
    switch (value) {
      case TWOX:
        return 1;
      case FOURX:
        return 2;
      case GRAY:
        return 3;
    }
    return 0;
  }

  private String getStr(int value) {
    if (value == NONE)
      return "1X";
    else if (value == FOURX)
      return "4X";
    else if (value == TWOX)
      return "2X";
    else if (value == GRAY)
      return "Gray";
    else
      return null;
  }

  public synchronized String getDefaultStr() { return getStr(defValue); }
  public synchronized String getStr() { return getStr(value); }

  public String getValues() { return "1X, 2X, 4X, Gray"; }
};
