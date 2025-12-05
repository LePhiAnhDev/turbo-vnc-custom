package com.turbovnc.rfb;

public abstract class VoidParameter {

  public VoidParameter(String name_, Params params_, boolean isGUI_,
                       boolean advanced_, String desc_) {
    name = name_;
    params = params_;
    desc = desc_;
    isGUI = isGUI_;
    advanced = advanced_;
    if (params.head == null)
      params.head = this;
    if (params.tail != null)
      params.tail.next = this;
    params.tail = this;
  }

  public final String getName() { return name; }
  public final String getDescription() { return desc; }

  public abstract boolean set(String str);

  public final boolean set(String str, boolean commandLine_) {
    setCommandLine(commandLine_);
    boolean retval = set(str);
    setCommandLine(commandLine_);
    return retval;
  }

  public final void setCommandLine(boolean commandLine_) {
    commandLine = commandLine_;
  }

  public abstract void reset();

  public abstract boolean setDefault(String str);

  public abstract String getStr();
  public abstract String getDefaultStr();
  public abstract String getValues();
  public boolean isBool() { return false; }
  public boolean isCommandLine() { return commandLine; }
  public final boolean isGUI() { return isGUI; }
  public final boolean isAdvanced() { return advanced; }
  public final VoidParameter next() { return next; }

  private VoidParameter next;
  private final String name, desc;
  final Params params;
  private final boolean isGUI;
  private final boolean advanced;
  private boolean commandLine;
}
