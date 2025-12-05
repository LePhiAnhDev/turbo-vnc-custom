package com.turbovnc.vncviewer;

public final class VncSession {

  @Override
  public String toString() {
    return display;
  }

  public VncSession(String display_, String udsPath_) {
    display = display_;
    udsPath = udsPath_;
  }

  String display;
  String udsPath;
  int sessionLimit;
}
