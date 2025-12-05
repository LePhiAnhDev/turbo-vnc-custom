package com.jcraft.jsch;

/** Use PBKDF2 instead. */
@Deprecated
public interface PBKDF {
  byte[] getKey(byte[] pass, byte[] salt, int iteration, int size);
}
