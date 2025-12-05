package com.jcraft.jsch;

public interface Cipher {
  static int ENCRYPT_MODE = 0;
  static int DECRYPT_MODE = 1;

  int getIVSize();

  int getBlockSize();

  default int getTagSize() {
    return 0;
  }

  void init(int mode, byte[] key, byte[] iv) throws Exception;

  default void update(int foo) throws Exception {}

  void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception;

  default void updateAAD(byte[] foo, int s1, int len) throws Exception {}

  default void doFinal(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception {}

  boolean isCBC();

  default boolean isAEAD() {
    return false;
  }

  default boolean isChaCha20() {
    return false;
  }
}
