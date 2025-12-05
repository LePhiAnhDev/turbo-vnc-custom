package com.turbovnc.rdr;

import java.io.*;

public class FileInStream extends InStream {

  static final int BUFSIZE = 131072;

  static final double getTime() {
    return (double)System.nanoTime() / 1.0e9;
  }

  public FileInStream(String fileName) throws FileNotFoundException {
    fis = new FileInputStream(fileName);
    b = new byte[BUFSIZE];
    ptr = end = offset = 0;
  }

  public double getReadTime() { return tRead; }
  public void resetReadTime() { tRead = 0.0; }

  public void reset() {
    try {
      fis.getChannel().position(0);
    } catch (IOException e) {}
    ptr = end = offset = 0;
  }

  protected int overrun(int itemSize, int nItems, boolean wait) {
    if (itemSize > BUFSIZE)
      throw new ErrorException("FileInStream overrun: max itemSize exceeded");

    double tReadStart = getTime();

    if (end - ptr != 0)
      System.arraycopy(b, ptr, b, 0, end - ptr);

    offset += ptr;
    end -= ptr;
    ptr = 0;

    while (end < itemSize) {
      int n = 0;
      try {
        n = fis.read(b, end, BUFSIZE - end);
      } catch (IOException e) {
        throw new ErrorException("Read error: " + e.getMessage());
      }
      if (n < 1) {
        tRead += getTime() - tReadStart;
        if (n < 0) {  throw new EndOfStream(); }
        if (n == 0) { return 0; }
      }
      end += n;
    }

    if (itemSize * nItems > end - ptr)
      nItems = (end - ptr) / itemSize;

    tRead += getTime() - tReadStart;

    return nItems;
  }

  public final int pos() { return offset + ptr; }

  FileInputStream fis;
  double tRead;
  int offset;
};
