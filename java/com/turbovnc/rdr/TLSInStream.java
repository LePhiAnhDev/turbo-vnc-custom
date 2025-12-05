package com.turbovnc.rdr;

import java.nio.channels.*;
import javax.net.ssl.*;

import com.turbovnc.network.*;

public class TLSInStream extends InStream {

  static final int DEFAULT_BUF_SIZE = 16384;

  public TLSInStream(InStream in_, SSLEngineManager manager_) {
    in = (FdInStream)in_;
    manager = manager_;
    offset = 0;
    SSLSession session = manager.getSession();
    bufSize = session.getApplicationBufferSize();
    b = new byte[bufSize];
    ptr = end = start = 0;
  }

  public final int pos() {
    return offset + ptr - start;
  }

  public final void startTiming() {
    in.startTiming();
  }

  public final void stopTiming() {
    in.stopTiming();
  }

  public final long kbitsPerSecond() {
    return in.kbitsPerSecond();
  }

  public final long timeWaited() {
    return in.timeWaited();
  }

  protected final int overrun(int itemSize, int nItems, boolean wait) {
    if (itemSize > bufSize)
      throw new ErrorException("TLSInStream overrun: max itemSize exceeded");

    if (end - ptr != 0)
      System.arraycopy(b, ptr, b, 0, end - ptr);

    offset += ptr - start;
    end -= ptr - start;
    ptr = start;

    while (end < start + itemSize) {
      int n = readTLS(b, end, start + bufSize - end, wait);
      if (!wait && n == 0)
        return 0;
      end += n;
    }

    if (itemSize * nItems > end - ptr)
      nItems = (end - ptr) / itemSize;

    return nItems;
  }

  protected int readTLS(byte[] buf, int bufPtr, int len, boolean wait) {
    int n = -1;

    //n = in.check(1, 1, wait);
    //if (n == 0)
    //  return 0;

    try {
      n = manager.read(buf, bufPtr, len);
    } catch (java.io.IOException e) {
      throw new ErrorException("TLS read error: " + e.getMessage());
    }

    if (n < 0) throw new ErrorException("TLS read error");

    return n;
  }

  private SSLEngineManager manager;
  private int offset;
  private int start;
  private int bufSize;
  private FdInStream in;
}
