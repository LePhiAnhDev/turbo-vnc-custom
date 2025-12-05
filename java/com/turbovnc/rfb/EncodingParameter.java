package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public final class EncodingParameter extends IntParameter {

  public EncodingParameter(String name, Params params, String desc,
                           int defValue) {
    super(name, params, false, false, desc, defValue, 0, RFB.ENCODING_LAST);
  }

  public boolean set(String encString) {
    int encNum = RFB.encodingNum(encString);
    if (encNum == -1)
      throw new ErrorException(getName() + " parameter is incorrect");
    return set(encNum);
  }

  public boolean setDefault(String encString) {
    int encNum = RFB.encodingNum(encString);
    if (encNum == -1)
      return false;
    return super.setDefault(encNum);
  }

  public synchronized String getDefaultStr() {
    return RFB.encodingName(defValue);
  }

  public synchronized String getStr() {
    return RFB.encodingName(value);
  }

  public String getValues() { return "Tight, ZRLE, Hextile, Raw"; }
}
