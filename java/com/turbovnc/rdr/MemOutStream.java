package com.turbovnc.rdr;

public class MemOutStream extends OutStream {

  public MemOutStream(int len) {
    b = new byte[len];
    ptr = 0;
    end = len;
  }
  public MemOutStream() { this(1024); }

  public int length() { return ptr; }
  public void clear() { ptr = 0; };
  public void reposition(int pos) { ptr = pos; }

  // data() returns a pointer to the buffer.

  public final byte[] data() { return b; }

  // overrun() either doubles the buffer or adds enough space for nItems of
  // size itemSize bytes.

  protected int overrun(int itemSize, int nItems) {
    int len = ptr + itemSize * nItems;
    if (len < end * 2)
      len = end * 2;

    byte[] newBuf = new byte[len];
    System.arraycopy(b, 0, newBuf, 0, ptr);
    b = newBuf;
    end = len;

    return nItems;
  }
}
