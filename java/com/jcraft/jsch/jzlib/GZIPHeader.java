package com.jcraft.jsch.jzlib;

import java.nio.charset.StandardCharsets;

final class GZIPHeader implements Cloneable {

  static final byte OS_MSDOS = (byte) 0x00;
  static final byte OS_AMIGA = (byte) 0x01;
  static final byte OS_VMS = (byte) 0x02;
  static final byte OS_UNIX = (byte) 0x03;
  static final byte OS_ATARI = (byte) 0x05;
  static final byte OS_OS2 = (byte) 0x06;
  static final byte OS_MACOS = (byte) 0x07;
  static final byte OS_TOPS20 = (byte) 0x0a;
  static final byte OS_WIN32 = (byte) 0x0b;
  static final byte OS_VMCMS = (byte) 0x04;
  static final byte OS_ZSYSTEM = (byte) 0x08;
  static final byte OS_CPM = (byte) 0x09;
  static final byte OS_QDOS = (byte) 0x0c;
  static final byte OS_RISCOS = (byte) 0x0d;
  static final byte OS_UNKNOWN = (byte) 0xff;

  boolean text = false;
  private boolean fhcrc = false;
  int xflags;
  int os = 255;
  byte[] extra;
  byte[] name;
  byte[] comment;
  int hcrc;
  long crc;
  boolean done = false;
  long mtime = 0;

  void setModifiedTime(long mtime) {
    this.mtime = mtime;
  }

  long getModifiedTime() {
    return mtime;
  }

  void setOS(int os) {
    if ((0 <= os && os <= 13) || os == 255)
      this.os = os;
    else
      throw new IllegalArgumentException("os: " + os);
  }

  int getOS() {
    return os;
  }

  void setName(String name) {
    this.name = name.getBytes(StandardCharsets.ISO_8859_1);
  }

  String getName() {
    if (name == null)
      return "";
    return new String(name, StandardCharsets.ISO_8859_1);
  }

  void setComment(String comment) {
    this.comment = comment.getBytes(StandardCharsets.ISO_8859_1);
  }

  String getComment() {
    if (comment == null)
      return "";
    return new String(comment, StandardCharsets.ISO_8859_1);
  }

  void setCRC(long crc) {
    this.crc = crc;
  }

  long getCRC() {
    return crc;
  }

  void put(Deflate d) {
    int flag = 0;
    if (text) {
      flag |= 1;
    }
    if (fhcrc) {
      flag |= 2;
    }
    if (extra != null) {
      flag |= 4;
    }
    if (name != null) {
      flag |= 8;
    }
    if (comment != null) {
      flag |= 16;
    }
    int xfl = 0;
    if (d.level == JZlib.Z_BEST_SPEED) {
      xfl |= 4;
    } else if (d.level == JZlib.Z_BEST_COMPRESSION) {
      xfl |= 2;
    }

    d.put_short((short) 0x8b1f);
    d.put_byte((byte) 8);
    d.put_byte((byte) flag);
    d.put_byte((byte) mtime);
    d.put_byte((byte) (mtime >> 8));
    d.put_byte((byte) (mtime >> 16));
    d.put_byte((byte) (mtime >> 24));
    d.put_byte((byte) xfl);
    d.put_byte((byte) os);

    if (extra != null) {
      d.put_byte((byte) extra.length);
      d.put_byte((byte) (extra.length >> 8));
      d.put_byte(extra, 0, extra.length);
    }

    if (name != null) {
      d.put_byte(name, 0, name.length);
      d.put_byte((byte) 0);
    }

    if (comment != null) {
      d.put_byte(comment, 0, comment.length);
      d.put_byte((byte) 0);
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    GZIPHeader gheader = (GZIPHeader) super.clone();
    byte[] tmp;
    if (gheader.extra != null) {
      tmp = new byte[gheader.extra.length];
      System.arraycopy(gheader.extra, 0, tmp, 0, tmp.length);
      gheader.extra = tmp;
    }

    if (gheader.name != null) {
      tmp = new byte[gheader.name.length];
      System.arraycopy(gheader.name, 0, tmp, 0, tmp.length);
      gheader.name = tmp;
    }

    if (gheader.comment != null) {
      tmp = new byte[gheader.comment.length];
      System.arraycopy(gheader.comment, 0, tmp, 0, tmp.length);
      gheader.comment = tmp;
    }

    return gheader;
  }
}
