package com.jcraft.jsch;

public interface KDF {
  byte[] getKey(byte[] pass, int size);
}
