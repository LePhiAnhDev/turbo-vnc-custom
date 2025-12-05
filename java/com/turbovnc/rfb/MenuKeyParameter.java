package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public final class MenuKeyParameter extends VoidParameter {

  public MenuKeyParameter(String name, Params params, String desc,
                          String defValue_) {
    super(name, params, true, false, desc);
    defValue = defValue_;
    set(defValue);
  }

  public synchronized boolean set(String menuKeyString) {
    symbol = MenuKey.getSymbol(menuKeyString);
    if (symbol == null)
      throw new ErrorException(getName() + " parameter is incorrect");
    setCommandLine(false);
    return true;
  }

  public void reset() { set(defValue); }

  public synchronized boolean setDefault(String menuKeyString) {
    symbol = MenuKey.getSymbol(menuKeyString);
    if (symbol == null)
      return false;
    defValue = symbol.name;
    set(defValue);
    return true;
  }

  public int getVKeyCode() { return symbol.vKeyCode; }
  public int getKeySym() { return symbol.keysym; }
  public int getRFBKeyCode() { return symbol.rfbKeyCode; }

  public synchronized String getDefaultStr() { return defValue; }
  public synchronized String getStr() { return symbol.name; }

  public String getValues() { return MenuKey.getValueStr(); }

  private String defValue;
  private MenuKey.Symbol symbol;
};
