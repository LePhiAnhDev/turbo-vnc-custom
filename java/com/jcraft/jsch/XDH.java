package com.jcraft.jsch;

public interface XDH {
  void init(String name, int keylen) throws Exception;

  byte[] getSecret(byte[] u) throws Exception;

  byte[] getQ() throws Exception;

  boolean validate(byte[] u) throws Exception;
}
