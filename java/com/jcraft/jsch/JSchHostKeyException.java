package com.jcraft.jsch;

public abstract class JSchHostKeyException extends JSchException {
  private static final long serialVersionUID = -1L;

  JSchHostKeyException() {
    super();
  }

  JSchHostKeyException(String s) {
    super(s);
  }
}
