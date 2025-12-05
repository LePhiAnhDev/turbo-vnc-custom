package com.turbovnc.rfb;

public class RawDecoder extends Decoder {

  public RawDecoder(CMsgReader reader_) { reader = reader_; }

  public void readRect(Rect r, CMsgHandler handler) {
    int[] stride = { r.width() };
    Object buf = handler.getRawPixelsRW(stride);

    reader.getInStream().readPixels(buf, stride[0], r, (reader.bpp() / 8),
                                    handler.cp.pf().bigEndian);
    handler.releaseRawPixels(r);
  }

  CMsgReader reader;
  static LogWriter vlog = new LogWriter("RawDecoder");
}
