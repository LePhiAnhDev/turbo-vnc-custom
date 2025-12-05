package com.turbovnc.rfb;

public class ExtInputEvent {
  // CHECKSTYLE VisibilityModifier:OFF
  public int type;
  public long deviceID;
  public long buttonMask;
  public int buttonNumber;
  public int numValuators;
  public int firstValuator;
  public int[] valuators = new int[6];
  // CHECKSTYLE VisibilityModifier:ON

  public void print() {
    vlog.eidebug("EVENT:");
    vlog.eidebug("  type = " + RFB.giiEventName(type));
    vlog.eidebug("  deviceID = " + deviceID);
    vlog.eidebug("  buttonMask = " + buttonMask);
    if (type == RFB.GII_BUTTON_PRESS || type == RFB.GII_BUTTON_RELEASE)
      vlog.eidebug("  buttonNumber = " + buttonNumber);
    vlog.eidebug("  firstValuator = " + firstValuator);
    for (int i = 0; i < numValuators; i++)
      vlog.eidebug("    Valuator " + (i + firstValuator) + " = " +
                   valuators[i]);
  }

  static LogWriter vlog = new LogWriter("ExtInputEvent");
};
