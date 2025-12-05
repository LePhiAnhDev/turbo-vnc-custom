package com.jcraft.jsch.juz;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;
import java.util.function.Supplier;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compression implements com.jcraft.jsch.Compression {
  private static final int BUF_SIZE = 4096;
  private final int buffer_margin = 32 + 20;
  private Deflater deflater;
  private Inflater inflater;
  private byte[] tmpbuf = new byte[BUF_SIZE];
  private byte[] inflated_buf;
  private Session session;

  public Compression() {}

  private void logMessage(int level, Supplier<String> message) {
    Logger logger = session == null ? JSch.getLogger() : session.getLogger();
    if (!logger.isEnabled(level)) {
      return;
    }
    logger.log(level, message.get());
  }

  @Override
  public void end() {
    inflated_buf = null;
    if (inflater != null) {
      inflater.end();
      inflater = null;
    }
    if (deflater != null) {
      deflater.end();
      deflater = null;
    }
    session = null;
  }

  @Override
  public void init(int type, int level, Session session) {
    this.session = session;
    init(type, level);
  }

  @Override
  public void init(int type, int level) {
    if (type == DEFLATER) {
      deflater = new Deflater(level);
    } else if (type == INFLATER) {
      inflater = new Inflater();
      inflated_buf = new byte[BUF_SIZE];
    }
    logMessage(Logger.DEBUG, () -> "zlib using " + this.getClass().getCanonicalName());
  }

  @Override
  public byte[] compress(byte[] buf, int start, int[] end) {
    if (tmpbuf.length < end[0]) {
      tmpbuf = new byte[end[0] * 2];
    }

    deflater.setInput(buf, start, end[0] - start);

    byte[] obuf = buf;
    int obuflen = start;

    do {
      int result = deflater.deflate(tmpbuf, 0, tmpbuf.length, Deflater.SYNC_FLUSH);
      if (obuf.length < obuflen + result + buffer_margin) {
        byte[] tmp = new byte[(obuflen + result + buffer_margin) * 2];
        System.arraycopy(obuf, 0, tmp, 0, obuf.length);
        obuf = tmp;
      }
      System.arraycopy(tmpbuf, 0, obuf, obuflen, result);
      obuflen += result;
    } while (!deflater.needsInput());

    end[0] = obuflen;
    return obuf;
  }

  @Override
  public byte[] uncompress(byte[] buf, int start, int[] len) {
    inflater.setInput(buf, start, len[0]);

    int inflated_end = 0;
    try {
      do {
        int result = inflater.inflate(tmpbuf, 0, tmpbuf.length);
        if (inflated_buf.length < inflated_end + result) {
          byte[] tmp = new byte[inflated_end + result];
          System.arraycopy(inflated_buf, 0, tmp, 0, inflated_end);
          inflated_buf = tmp;
        }
        System.arraycopy(tmpbuf, 0, inflated_buf, inflated_end, result);
        inflated_end += result;
      } while (inflater.getRemaining() > 0);
    } catch (DataFormatException e) {
      logMessage(Logger.WARN, () -> "an exception during uncompress\n" + e.toString());
    }

    if (buf.length < inflated_buf.length + start) {
      byte[] tmp = new byte[inflated_buf.length + start];
      System.arraycopy(buf, 0, tmp, 0, start);
      buf = tmp;
    }

    System.arraycopy(inflated_buf, 0, buf, start, inflated_end);
    len[0] = inflated_end;
    return buf;
  }
}
