package com.turbovnc.rdr;

public class SystemException extends RuntimeException {
  public SystemException(Throwable e) {
    super(e);
  }

  public static final void checkException(Exception e) {
    Throwable cause = e.getCause();
    if (cause instanceof ErrorException)
      throw (ErrorException)cause;
    else if (cause instanceof WarningException)
      throw (WarningException)cause;
    else if (cause instanceof SystemException)
      throw (SystemException)cause;
    else if (e instanceof ErrorException)
      throw (ErrorException)e;
    else if (e instanceof WarningException)
      throw (WarningException)e;
    else if (e instanceof SystemException)
      throw (SystemException)e;
  }
}
