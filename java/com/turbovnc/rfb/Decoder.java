package com.turbovnc.rfb;

public abstract class Decoder {

  public abstract void readRect(Rect r, CMsgHandler handler);

  public void reset() {}

  public void close() {}

  public static boolean supported(int encoding) {
    return (encoding == RFB.ENCODING_RAW ||
            encoding == RFB.ENCODING_HEXTILE ||
            encoding == RFB.ENCODING_TIGHT ||
            encoding == RFB.ENCODING_ZRLE);
  }

  public static Decoder createDecoder(int encoding, CMsgReader reader) {
    switch (encoding) {
      case RFB.ENCODING_RAW:      return new RawDecoder(reader);
      case RFB.ENCODING_HEXTILE:  return new HextileDecoder(reader);
      case RFB.ENCODING_TIGHT:    return new TightDecoder(reader);
      case RFB.ENCODING_ZRLE:     return new ZRLEDecoder(reader);
    }
    return null;
  }
}
