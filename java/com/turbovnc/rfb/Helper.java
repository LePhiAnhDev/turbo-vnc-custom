package com.turbovnc.rfb;

public final class Helper {

  public static synchronized boolean isAvailable() {
    if (!triedInit) {
      try {
        System.loadLibrary("turbovnchelper");
        available = true;
      } catch (UnsatisfiedLinkError e) {
        vlog.info("WARNING: Could not find TurboVNC Helper JNI library.  If it is in a");
        vlog.info("  non-standard location, then add -Djava.library.path=<dir>");
        vlog.info("  to the Java command line to specify its location.");
        printMissingFeatures();
      } catch (Exception e) {
        vlog.info("WARNING: Could not initialize TurboVNC Helper JNI library:");
        vlog.info("  " + e.toString());
        printMissingFeatures();
      }
    }
    triedInit = true;
    return available;
  }

  private static synchronized void printMissingFeatures() {
    vlog.info("  The following features will be disabled:");
    vlog.info("  - Accelerated JPEG decompression");
    if (Utils.osGrab())
      vlog.info("  - Keyboard grabbing");
    if (Utils.osEID())
      vlog.info("  - Extended input device support");
    if (Utils.isX11())
      vlog.info("  - Multi-screen spanning in full-screen mode");
    if (Utils.isMac())
      vlog.info("  - Multi-screen spanning");
    if (Utils.isWindows())
      vlog.info("  - Pageant support");
    if (Utils.isX11() || Utils.isMac())
      vlog.info("  - Server-side keyboard mapping");
  }

  public static synchronized void setAvailable(boolean avail) {
    available = avail;
  }

  static boolean triedInit, available;

  private Helper() {}
  static LogWriter vlog = new LogWriter("Helper");
}
