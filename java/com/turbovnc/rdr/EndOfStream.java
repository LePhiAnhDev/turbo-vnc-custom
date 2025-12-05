package com.turbovnc.rdr;

public class EndOfStream extends WarningException {
  public EndOfStream() {
    super("Connection closed");
  }
}
