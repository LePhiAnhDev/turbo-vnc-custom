package com.turbovnc.rdr;

/* We use this to communicate exceptions that are generally the result of user
   actions. */

public class WarningException extends RuntimeException {
  public WarningException(String message) {
    super(message);
  }
}
