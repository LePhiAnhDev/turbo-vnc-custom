package com.jcraft.jsch;

public interface BCrypt extends KDF {
  void init(byte[] salt, int iteration) throws Exception;
}
