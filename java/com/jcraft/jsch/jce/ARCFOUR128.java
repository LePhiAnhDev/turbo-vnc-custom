package com.jcraft.jsch.jce;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ARCFOUR128 implements com.jcraft.jsch.Cipher {
  private static final int ivsize = 8;
  private static final int bsize = 16;
  private static final int skip = 1536;
  private Cipher cipher;

  @Override
  public int getIVSize() {
    return ivsize;
  }

  @Override
  public int getBlockSize() {
    return bsize;
  }

  @Override
  public void init(int mode, byte[] key, byte[] iv) throws Exception {
    byte[] tmp;
    if (key.length > bsize) {
      tmp = new byte[bsize];
      System.arraycopy(key, 0, tmp, 0, tmp.length);
      key = tmp;
    }
    try {
      cipher = Cipher.getInstance("RC4");
      SecretKeySpec _key = new SecretKeySpec(key, "RC4");
      cipher.init(
          (mode == com.jcraft.jsch.Cipher.ENCRYPT_MODE ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE),
          _key);
      byte[] foo = new byte[1];
      for (int i = 0; i < skip; i++) {
        cipher.update(foo, 0, 1, foo, 0);
      }
    } catch (Exception e) {
      cipher = null;
      throw e;
    }
  }

  @Override
  public void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception {
    cipher.update(foo, s1, len, bar, s2);
  }

  @Override
  public boolean isCBC() {
    return false;
  }
}
