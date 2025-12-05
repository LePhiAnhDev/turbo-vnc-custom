package com.turbovnc.vncviewer;

public interface OptionsDialogCallback {
  void setOptions();
  void setTightOptions();
  void getOptions();
  boolean supportsSetDesktopSize();
}
