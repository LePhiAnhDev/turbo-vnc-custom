package com.turbovnc.rfb;

public class Cursor extends ManagedPixelBuffer {

  public void setSize(int w, int h) {
    super.setSize(w, h);
    if (mask == null || mask.length < maskLen())
      mask = new byte[maskLen()];
  }
  public int maskLen() { return (width() + 7) / 8 * height(); }

  // CHECKSTYLE VisibilityModifier:OFF
  public Point hotspot;
  public byte[] mask;
  // CHECKSTYLE VisibilityModifier:ON
}
