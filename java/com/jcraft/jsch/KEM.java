package com.jcraft.jsch;

public interface KEM {
  void init() throws Exception;

  byte[] getPublicKey() throws Exception;

  byte[] decapsulate(byte[] encapsulation) throws Exception;
}
