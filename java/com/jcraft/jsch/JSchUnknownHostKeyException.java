package com.jcraft.jsch;

public class JSchUnknownHostKeyException extends JSchHostKeyException {
  private static final long serialVersionUID = -1L;

  JSchUnknownHostKeyException() {
    super();
  }

  JSchUnknownHostKeyException(String s) {
    super(s);
  }
}
