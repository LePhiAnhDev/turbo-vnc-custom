package com.turbovnc.rfb;

public abstract class CSecurity {
  public abstract boolean processMsg(CConnection cc);
  public abstract int getType();
  public int getChosenType() { return getType(); }
  public abstract String getDescription();
  public abstract String getProtocol();

  static UserPasswdGetter upg;
}
