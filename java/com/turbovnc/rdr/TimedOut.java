package com.turbovnc.rdr;

class TimedOut extends ErrorException {
  TimedOut() {
    super("Timed out");
  }
}
