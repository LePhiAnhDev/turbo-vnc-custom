package com.jcraft.jsch;

public interface PBKDF2 extends KDF {
  void init(byte[] salt, int iteration) throws Exception;
}
