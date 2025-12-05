package com.turbovnc.rfb;

public final class Utils {

  private static int getJavaVersion() {
    String javaVersionString = System.getProperty("java.version");
    javaVersionString = javaVersionString.split("-")[0];
    String[] javaVersionStrings = javaVersionString.split("\\.");
    int majorVersion = Integer.parseInt(javaVersionStrings[0]);
    return (majorVersion <= 1 && javaVersionStrings.length > 1 ?
            Integer.parseInt(javaVersionStrings[1]) : majorVersion);
  }

  public static final int JAVA_VERSION = getJavaVersion();

  // Windows-only platform detection
  public static boolean isMac() {
    return false;
  }

  public static boolean isWindows() {
    return true;
  }

  public static boolean isX11() {
    return false;
  }

  public static boolean osEID() {
    return false;  // Windows does not support Extended Input Device
  }

  public static boolean osGrab() {
    return true;  // Windows supports pointer/keyboard grab
  }

  private static native boolean displaysHaveSeparateSpaces();

  public static boolean displaysHaveSeparateSpacesHelper() {
    return false;  // Windows does not have separate spaces per display
  }

  public static boolean getBooleanProperty(String key, boolean def) {
    String prop = System.getProperty(key, def ? "True" : "False");
    if (prop != null && prop.length() > 0) {
      if (prop.equalsIgnoreCase("true") || prop.equalsIgnoreCase("yes"))
        return true;
      if (prop.equalsIgnoreCase("false") || prop.equalsIgnoreCase("no"))
        return false;
      int i = -1;
      try {
        i = Integer.parseInt(prop);
      } catch (NumberFormatException e) {}
      if (i == 1)
        return true;
      if (i == 0)
        return false;
    }
    return def;
  }

  public static int getIntProperty(String key) {
    String prop = System.getProperty(key);
    if (prop != null && prop.length() > 0) {
      int i = -1;
      try {
        i = Integer.parseInt(prop);
      } catch (NumberFormatException e) {}
      return i;
    }
    return -1;
  }

  public static String getFileSeparator() {
    String separator = null;
    try {
      separator = Character.toString(java.io.File.separatorChar);
    } catch (Exception e) {
      vlog.error("Cannot access file.separator system property:");
      vlog.error("  " + e.getMessage());
    }
    return separator;
  }

  public static String getHomeDir() {
    String homeDir = null;
    try {
      // Windows-specific: use USERPROFILE environment variable
      homeDir = System.getenv("USERPROFILE");
      if (homeDir == null) {
        vlog.error("Cannot access USERPROFILE environment variable");
      }
    } catch (Exception e) {
      vlog.error("Cannot access system property:");
      vlog.error("  " + e.getMessage());
      e.printStackTrace();
    }

    return homeDir != null ? (homeDir + getFileSeparator()) : "";
  }

  public static String getVncHomeDir() {
    return getHomeDir() + ".vnc" + getFileSeparator();
  }

  public static double getTime() {
    return (double)System.nanoTime() / 1.0e9;
  }

  public static String convertCRLF(String buf) {
    return convertLF(buf).replaceAll("\\n", "\r\n");
  }

  public static String convertLF(String buf) {
    return buf.replaceAll("\\r\\n?", "\n");
  }

  private Utils() {}
  static LogWriter vlog = new LogWriter("Utils");
}
