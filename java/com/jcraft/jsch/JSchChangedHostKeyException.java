package com.jcraft.jsch;

public class JSchChangedHostKeyException extends JSchHostKeyException {
  private static final long serialVersionUID = -1L;

  JSchChangedHostKeyException() {
    super();
  }

  JSchChangedHostKeyException(String s) {
    super(s);
  }
}
