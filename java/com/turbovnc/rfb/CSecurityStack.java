package com.turbovnc.rfb;

public class CSecurityStack extends CSecurity {

  public CSecurityStack(int type_, String name_, CSecurity s0,
                        CSecurity s1) {
    name = name_;
    type = type_;
    state = 0;
    state0 = s0;
    state1 = s1;
  }

  public boolean processMsg(CConnection cc) {
    boolean res = true;
    if (state == 0) {
      if (state0 != null)
        res = state0.processMsg(cc);

      if (!res)
        return res;

      state++;
    }

    if (state == 1) {
      if (state1 != null)
        res = state1.processMsg(cc);

      if (!res)
        return res;

      state++;
    }

    return res;
  }

  public final int getType() { return type; }
  public final String getDescription() { return name; }

  public final String getProtocol() {
    return (state0 != null ? state0.getProtocol() : "None");
  }

  private int state;
  private CSecurity state0;
  private CSecurity state1;
  private String name;
  private int type;
}
