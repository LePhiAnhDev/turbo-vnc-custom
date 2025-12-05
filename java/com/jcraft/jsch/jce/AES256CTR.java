package com.jcraft.jsch.jce;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256CTR implements com.jcraft.jsch.Cipher {
  private static final int ivsize = 16;
  private static final int bsize = 32;
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
    if (iv.length > ivsize) {
      tmp = new byte[ivsize];
      System.arraycopy(iv, 0, tmp, 0, tmp.length);
      iv = tmp;
    }
    if (key.length > bsize) {
      tmp = new byte[bsize];
      System.arraycopy(key, 0, tmp, 0, tmp.length);
      key = tmp;
    }
    try {
      SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
      cipher = Cipher.getInstance("AES/CTR/NoPadding");
      cipher.init(
          (mode == com.jcraft.jsch.Cipher.ENCRYPT_MODE ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE),
          keyspec, new IvParameterSpec(iv));
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
