package com.jcraft.jsch;

class JSchPartialAuthException extends JSchException {
  private static final long serialVersionUID = -1L;
  String methods;

  public JSchPartialAuthException() {
    super();
  }

  public JSchPartialAuthException(String s) {
    super(s);
    this.methods = s;
  }

  public String getMethods() {
    return methods;
  }
}
