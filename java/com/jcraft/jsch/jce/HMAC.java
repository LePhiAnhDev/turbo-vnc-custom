package com.jcraft.jsch.jce;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.MAC;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

abstract class HMAC implements MAC {
  protected String name;
  protected int bsize;
  protected String algorithm;
  protected boolean etm;
  private Mac mac;

  @Override
  public int getBlockSize() {
    return bsize;
  };

  @Override
  public void init(byte[] key) throws Exception {
    if (key.length > bsize) {
      byte[] tmp = new byte[bsize];
      System.arraycopy(key, 0, tmp, 0, bsize);
      key = tmp;
    }
    SecretKeySpec skey = new SecretKeySpec(key, algorithm);
    mac = Mac.getInstance(algorithm);
    mac.init(skey);
  }

  private final byte[] tmp = new byte[4];

  @Override
  public void update(int i) {
    tmp[0] = (byte) (i >>> 24);
    tmp[1] = (byte) (i >>> 16);
    tmp[2] = (byte) (i >>> 8);
    tmp[3] = (byte) i;
    update(tmp, 0, 4);
  }

  @Override
  public void update(byte foo[], int s, int l) {
    mac.update(foo, s, l);
  }

  @Override
  public void doFinal(byte[] buf, int offset) {
    try {
      mac.doFinal(buf, offset);
    } catch (ShortBufferException e) {
      if (JSch.getLogger().isEnabled(Logger.ERROR)) {
        JSch.getLogger().log(Logger.ERROR, e.getMessage(), e);
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isEtM() {
    return etm;
  }
}
