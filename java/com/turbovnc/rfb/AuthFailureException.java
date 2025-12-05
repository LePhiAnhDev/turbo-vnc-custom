package com.turbovnc.rfb;

import com.turbovnc.rdr.WarningException;

public class AuthFailureException extends WarningException {
  public AuthFailureException(String s) { super(s); }
}
