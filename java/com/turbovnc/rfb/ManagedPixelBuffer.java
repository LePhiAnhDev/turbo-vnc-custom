package com.turbovnc.rfb;

public class ManagedPixelBuffer extends PixelBuffer {
  public void setSize(int w, int h) {
    width = w;
    height = h;
    checkDataSize();
  }
  public void setPF(PixelFormat pf) {
    super.setPF(pf);
    checkDataSize();
  }

  public int dataLen() { return area(); }

  final void checkDataSize() {
    if (data == null || ((int[])data).length < dataLen())
      data = new int[dataLen()];
  }
}
