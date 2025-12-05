package com.jcraft.jsch.jce;

import com.jcraft.jsch.HASH;
import java.security.MessageDigest;

public class MD5 implements HASH {
  MessageDigest md;

  @Override
  public int getBlockSize() {
    return 16;
  }

  @Override
  public void init() throws Exception {
    md = MessageDigest.getInstance("MD5");
  }

  @Override
  public void update(byte[] foo, int start, int len) throws Exception {
    md.update(foo, start, len);
  }

  @Override
  public byte[] digest() throws Exception {
    return md.digest();
  }

  @Override
  public String name() {
    return "MD5";
  }
}
