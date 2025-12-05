package com.jcraft.jsch.jce;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class TripleDESCTR implements com.jcraft.jsch.Cipher {
  private static final int ivsize = 8;
  private static final int bsize = 24;
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
      cipher = Cipher.getInstance("DESede/CTR/NoPadding");

      DESedeKeySpec keyspec = new DESedeKeySpec(key);
      SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
      SecretKey _key = keyfactory.generateSecret(keyspec);
      cipher.init(
          (mode == com.jcraft.jsch.Cipher.ENCRYPT_MODE ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE),
          _key, new IvParameterSpec(iv));
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
