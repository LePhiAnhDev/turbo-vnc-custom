package com.jcraft.jsch.jce;

import java.security.SecureRandom;

public class Random implements com.jcraft.jsch.Random {
  private byte[] tmp = new byte[16];
  private SecureRandom random = null;

  public Random() {
    random = new SecureRandom();
  }

  @Override
  public void fill(byte[] foo, int start, int len) {
    if (len > tmp.length) {
      tmp = new byte[len];
    }
    random.nextBytes(tmp);
    System.arraycopy(tmp, 0, foo, start, len);
  }
}
