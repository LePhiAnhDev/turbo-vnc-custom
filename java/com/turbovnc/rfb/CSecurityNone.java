package com.turbovnc.rfb;

public class CSecurityNone extends CSecurity {

  public boolean processMsg(CConnection cc) { return true; }
  public final int getType() { return RFB.SECTYPE_NONE; }
  public final String getDescription() { return "None"; }
  public final String getProtocol() { return "None"; }
  static LogWriter vlog = new LogWriter("CSecurityNone");
}
