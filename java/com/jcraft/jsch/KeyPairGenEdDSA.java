package com.jcraft.jsch;

public interface KeyPairGenEdDSA {
  void init(String name, int keylen) throws Exception;

  byte[] getPub();

  byte[] getPrv();

  default void init(String name, byte[] prv) throws Exception {
    throw new UnsupportedOperationException();
  }
}
