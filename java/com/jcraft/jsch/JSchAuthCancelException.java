package com.jcraft.jsch;

class JSchAuthCancelException extends JSchException {
  private static final long serialVersionUID = -1L;
  String method;

  JSchAuthCancelException() {
    super();
  }

  JSchAuthCancelException(String s) {
    super(s);
    this.method = s;
  }

  public String getMethod() {
    return method;
  }
}
