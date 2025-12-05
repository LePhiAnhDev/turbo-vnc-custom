package com.turbovnc.rfb;

public class LogWriter {

  public LogWriter(String name_) {
    name = name_;
    level = globalLogLevel;
    next = logWriters;
    logWriters = this;
  }

  public void setLevel(int level_) { level = level_; }

  public void write(int level_, String str) {
    if (level_ <= level) {
      System.err.println(name + ": " + str);
    }
  }

  public void error(String str) { write(0, str); }
  public void status(String str) { write(10, str); }
  public void info(String str) { write(30, str); }
  public void debug(String str) { write(100, str); }
  public void sshdebug(String str) { write(110, str); }
  public void eidebug(String str) { write(150, str); }

  public static boolean setLogParams(String params) {
    globalLogLevel = Integer.parseInt(params);
    LogWriter current = logWriters;
    while (current != null) {
      current.setLevel(globalLogLevel);
      current = current.next;
    }
    return true;
  }

  static LogWriter getLogWriter(String name) {
    LogWriter current = logWriters;
    while (current != null) {
      if (name.equalsIgnoreCase(current.name)) return current;
      current = current.next;
    }
    return null;
  }

  String name;
  int level;
  LogWriter next;
  static LogWriter logWriters;
  static int globalLogLevel = 30;
}
