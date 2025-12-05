package com.jcraft.jsch;

public interface KeyPairGenECDSA {
  void init(int key_size) throws Exception;

  byte[] getD();

  byte[] getR();

  byte[] getS();
}
