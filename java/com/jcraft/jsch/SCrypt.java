package com.jcraft.jsch;

public interface SCrypt extends KDF {
  void init(byte[] salt, int cost, int blocksize, int parallel) throws Exception;
}
