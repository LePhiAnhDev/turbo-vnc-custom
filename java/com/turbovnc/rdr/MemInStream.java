package com.turbovnc.rdr;

public class MemInStream extends InStream {

  public MemInStream(byte[] data, int offset, int len) {
    b = data;
    start = offset;
    ptr = start;
    end = start + len;
  }

  public int pos() { return ptr; }
  public void reposition(int pos) { ptr = start + pos; }

  protected int overrun(int itemSize, int nItems, boolean wait) {
    throw new EndOfStream();
  }

  int start;
}
