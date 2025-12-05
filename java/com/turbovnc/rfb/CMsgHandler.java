package com.turbovnc.rfb;

public abstract class CMsgHandler {

  public CMsgHandler() {
    cp = new ConnParams();
  }

  public void setDesktopSize(int width, int height) {
    cp.width = width;
    cp.height = height;
  }

  public void setExtendedDesktopSize(int reason, int result, int width,
                                     int height, ScreenSet layout) {
    cp.supportsSetDesktopSize = true;

    if (reason == RFB.EDS_REASON_CLIENT && result != RFB.EDS_RESULT_SUCCESS)
      return;

    if (!layout.validate(width, height, true))
      vlog.error("Server sent us an invalid screen layout");

    cp.width = width;
    cp.height = height;
    cp.screenLayout = layout;
  }

  public void setPixelFormat(PixelFormat pf) {
    cp.setPF(pf);
  }

  public void setName(String name) {
    cp.setName(name);
  }

  public void fence(int flags, int len, byte[] data) {
    cp.supportsFence = true;
  }

  void handleClipboardCaps(int flags, int[] lengths)
  {
    int i, num;

    vlog.debug("Server clipboard capabilities:");
    num = 0;
    for (i = 0; i < 16; i++) {
      if ((flags & (1 << i)) != 0) {
        switch (1 << i) {
          case RFB.EXTCLIP_FORMAT_UTF8:
            vlog.debug("- Plain text (limit = " + lengths[num] + " bytes)");
            break;
          case RFB.EXTCLIP_FORMAT_RTF:
            vlog.debug("- Rich text (limit = " + lengths[num] + " bytes)");
            break;
          case RFB.EXTCLIP_FORMAT_HTML:
            vlog.debug("- HTML (limit = " + lengths[num] + " bytes)");
            break;
          case RFB.EXTCLIP_FORMAT_DIB:
            vlog.debug("- Images (limit = " + lengths[num] + " bytes)");
            break;
          case RFB.EXTCLIP_FORMAT_FILES:
            vlog.debug("- Files (limit = " + lengths[num] + " bytes)");
            break;
          default:
            vlog.debug("- Unknown format 0x" + Integer.toHexString(1 << i));
            continue;
        }
        num++;
      }
    }

    cp.setClipboardCaps(flags, lengths);
  }

  abstract void handleClipboardNotify(int flags);
  abstract void handleClipboardPeek(int flags);
  abstract void handleClipboardProvide(int flags, int[] lengths,
                                       byte[][] buffers);
  abstract void handleClipboardRequest(int flags);

  public void endOfContinuousUpdates() {
    cp.supportsContinuousUpdates = true;
  }

  public abstract void enableGII();
  public abstract void giiDeviceCreated(int deviceOrigin);

  public abstract void enableQEMUExtKeyEvent();
  public abstract void setLEDState(int state);

  public abstract void enableExtMouseButtons();

  public abstract void setCursor(int width, int height, Point hotspot,
                                 int[] data, byte[] mask);
  public abstract void serverInit();

  public abstract void framebufferUpdateStart();
  public abstract void framebufferUpdateEnd();
  public abstract void beginRect(Rect r, int encoding);
  public abstract void endRect(Rect r, int encoding);
  public abstract void startDecodeTimer();
  public abstract void stopDecodeTimer();

  public abstract void setColourMapEntries(int firstColour, int nColours,
                                           int[] rgbs);
  public abstract void bell();
  public abstract void serverCutText(String str);

  public abstract void fillRect(Rect r, int pix);
  public abstract void imageRect(Rect r, Object pixels);
  public abstract void copyRect(Rect r, int srcX, int srcY);

  public abstract Object getRawPixelsRW(int[] stride);
  public abstract void releaseRawPixels(Rect r);

  public abstract PixelFormat getPreferredPF();
  public abstract CSecurity getCurrentCSecurity();

  @SuppressWarnings("checkstyle:VisibilityModifier")
  public ConnParams cp;

  static LogWriter vlog = new LogWriter("CMsgHandler");
}
