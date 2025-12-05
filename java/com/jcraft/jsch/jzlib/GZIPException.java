package com.jcraft.jsch.jzlib;

import java.io.IOException;

final class GZIPException extends IOException {
  private static final long serialVersionUID = -1L;

  GZIPException() {
    super();
  }

  GZIPException(String s) {
    super(s);
  }
}
