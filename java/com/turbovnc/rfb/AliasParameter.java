package com.turbovnc.rfb;

public final class AliasParameter extends VoidParameter {

  public AliasParameter(String name, Params params, String desc,
                        VoidParameter param_) {
    super(name, params, false, param_.isAdvanced(), desc);
    param = param_;
  }

  public boolean set(String str) {
    boolean retval = param.set(str);
    param.setCommandLine(isCommandLine());
    return retval;
  }

  public void reset() { param.reset(); }

  public boolean setDefault(String str) { return param.setDefault(str); }

  public String getDefaultStr() { return null; }
  public String getStr() { return null; }
  public String getValues() { return null; }
  public boolean isBool() { return param.isBool(); }

  private final VoidParameter param;
}
