package com.jcraft.jsch;

public class JSchRevokedHostKeyException extends JSchHostKeyException {
  private static final long serialVersionUID = -1L;

  JSchRevokedHostKeyException() {
    super();
  }

  JSchRevokedHostKeyException(String s) {
    super(s);
  }
}
