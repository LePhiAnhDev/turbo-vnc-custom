package com.jcraft.jsch.jce;

import java.nio.ByteBuffer;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

abstract class AESGCM implements com.jcraft.jsch.Cipher {
  // Actually the block size, not IV size
  private static final int ivsize = 16;
  private static final int tagsize = 16;
  private Cipher cipher;
  private SecretKeySpec keyspec;
  private int mode;
  private ByteBuffer iv;
  private long initcounter;

  @Override
  public int getIVSize() {
    return ivsize;
  }

  @Override
  public int getTagSize() {
    return tagsize;
  }

  @Override
  public void init(int mode, byte[] key, byte[] iv) throws Exception {
    byte[] tmp;
    if (iv.length > 12) {
      tmp = new byte[12];
      System.arraycopy(iv, 0, tmp, 0, tmp.length);
      iv = tmp;
    }
    int bsize = getBlockSize();
    if (key.length > bsize) {
      tmp = new byte[bsize];
      System.arraycopy(key, 0, tmp, 0, tmp.length);
      key = tmp;
    }
    this.mode =
        ((mode == com.jcraft.jsch.Cipher.ENCRYPT_MODE) ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE);
    this.iv = ByteBuffer.wrap(iv);
    this.initcounter = this.iv.getLong(4);
    try {
      keyspec = new SecretKeySpec(key, "AES");
      cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(this.mode, keyspec, new GCMParameterSpec(tagsize * 8, iv));
    } catch (Exception e) {
      cipher = null;
      keyspec = null;
      this.iv = null;
      throw e;
    }
  }

  @Override
  public void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception {
    cipher.update(foo, s1, len, bar, s2);
  }

  @Override
  public void updateAAD(byte[] foo, int s1, int len) throws Exception {
    cipher.updateAAD(foo, s1, len);
  }

  @Override
  public void doFinal(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception {
    cipher.doFinal(foo, s1, len, bar, s2);
    long newcounter = iv.getLong(4) + 1;
    if (newcounter == initcounter) {
      throw new IllegalStateException("GCM IV would be reused");
    }
    iv.putLong(4, newcounter);
    cipher.init(mode, keyspec, new GCMParameterSpec(tagsize * 8, iv.array()));
  }

  @Override
  public boolean isCBC() {
    return false;
  }

  @Override
  public boolean isAEAD() {
    return true;
  }
}
