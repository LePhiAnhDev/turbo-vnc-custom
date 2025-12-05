package com.jcraft.jsch;

public interface Argon2 extends KDF {
  public static final int ARGON2D = 0;
  public static final int ARGON2I = 1;
  public static final int ARGON2ID = 2;
  public static final int V10 = 0x10;
  public static final int V13 = 0x13;

  void init(byte[] salt, int iteration, int type, byte[] additional, byte[] secret, int memory,
      int parallelism, int version) throws Exception;
}
