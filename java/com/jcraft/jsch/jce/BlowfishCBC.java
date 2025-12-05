package com.jcraft.jsch.jce;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BlowfishCBC implements com.jcraft.jsch.Cipher {
  private static final int ivsize = 8;
  private static final int bsize = 16;
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
      SecretKeySpec skeySpec = new SecretKeySpec(key, "Blowfish");
      cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");
      cipher.init(
          (mode == com.jcraft.jsch.Cipher.ENCRYPT_MODE ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE),
          skeySpec, new IvParameterSpec(iv));
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception {
    cipher.update(foo, s1, len, bar, s2);
  }

  @Override
  public boolean isCBC() {
    return true;
  }
}
