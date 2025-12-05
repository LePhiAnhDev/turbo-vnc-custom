package com.turbovnc.rfb;

import com.turbovnc.rdr.ErrorException;

public final class HeaderParameter extends VoidParameter {

  public HeaderParameter(String name, Params params, boolean advanced,
                         String desc) {
    super(name, params, false, advanced, desc);
  }

  public boolean set(String str) {
    throw new ErrorException("Cannot set header parameter");
  }

  public boolean setDefault(String str) {
    throw new ErrorException("Cannot set default value for header parameter");
  }

  public void reset() {
    throw new ErrorException("Cannot reset header parameter");
  }

  public String getDefaultStr() {
    throw new ErrorException("Cannot get default string for header parameter");
  }

  public String getStr() {
    throw new ErrorException("Cannot get string for header parameter");
  }

  public String getValues() {
    throw new ErrorException("Cannot get values for header parameter");
  }
}
