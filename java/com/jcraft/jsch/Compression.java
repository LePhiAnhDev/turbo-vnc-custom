package com.jcraft.jsch;

public interface Compression {
  public static final int INFLATER = 0;
  public static final int DEFLATER = 1;

  default void init(int type, int level, Session session) {
    init(type, level);
  }

  default void end() {}

  void init(int type, int level);

  byte[] compress(byte[] buf, int start, int[] len);

  byte[] uncompress(byte[] buf, int start, int[] len);
}
