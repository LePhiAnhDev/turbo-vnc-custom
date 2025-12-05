package com.jcraft.jsch.jce;

public class AES128GCM extends AESGCM {
  // Actually the key size, not block size
  private static final int bsize = 16;

  @Override
  public int getBlockSize() {
    return bsize;
  }
}
