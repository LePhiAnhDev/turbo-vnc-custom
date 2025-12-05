package com.turbovnc.rfb;

import com.turbovnc.rdr.ErrorException;

public class ConnFailedException extends ErrorException {
  public ConnFailedException(String s) { super(s); }
}
