package com.jcraft.jsch.jce;

public class AES256GCM extends AESGCM {
  // Actually the key size, not block size
  private static final int bsize = 32;

  @Override
  public int getBlockSize() {
    return bsize;
  }
}
