package com.jcraft.jsch;

import java.util.Locale;

public class HostKey {

  private static final byte[][] names =
      {Util.str2byte("ssh-dss"), Util.str2byte("ssh-rsa"), Util.str2byte("ecdsa-sha2-nistp256"),
          Util.str2byte("ecdsa-sha2-nistp384"), Util.str2byte("ecdsa-sha2-nistp521"),
          Util.str2byte("ssh-ed25519"), Util.str2byte("ssh-ed448")};

  public static final int UNKNOWN = -1;
  public static final int GUESS = 0;
  public static final int SSHDSS = 1;
  public static final int SSHRSA = 2;
  public static final int ECDSA256 = 3;
  public static final int ECDSA384 = 4;
  public static final int ECDSA521 = 5;
  public static final int ED25519 = 6;
  public static final int ED448 = 7;

  protected String marker;
  protected String host;
  protected int type;
  protected byte[] key;
  protected String comment;

  public HostKey(String host, byte[] key) throws JSchException {
    this(host, GUESS, key);
  }

  public HostKey(String host, int type, byte[] key) throws JSchException {
    this(host, type, key, null);
  }

  public HostKey(String host, int type, byte[] key, String comment) throws JSchException {
    this("", host, type, key, comment);
  }

  public HostKey(String marker, String host, int type, byte[] key, String comment)
      throws JSchException {
    this.marker = marker;
    this.host = host;
    if (type == GUESS) {
      if (key[8] == 'd') {
        this.type = SSHDSS;
      } else if (key[8] == 'r') {
        this.type = SSHRSA;
      } else if (key[8] == 'e' && key[10] == '2') {
        this.type = ED25519;
      } else if (key[8] == 'e' && key[10] == '4') {
        this.type = ED448;
      } else if (key[8] == 'a' && key[20] == '2') {
        this.type = ECDSA256;
      } else if (key[8] == 'a' && key[20] == '3') {
        this.type = ECDSA384;
      } else if (key[8] == 'a' && key[20] == '5') {
        this.type = ECDSA521;
      } else {
        throw new JSchException("invalid key type");
      }
    } else {
      this.type = type;
    }
    this.key = key;
    this.comment = comment;
  }

  public String getHost() {
    return host;
  }

  public String getType() {
    if (type == SSHDSS || type == SSHRSA || type == ED25519 || type == ED448 || type == ECDSA256
        || type == ECDSA384 || type == ECDSA521) {
      return Util.byte2str(names[type - 1]);
    }
    return "UNKNOWN";
  }

  protected static int name2type(String name) {
    for (int i = 0; i < names.length; i++) {
      if (Util.byte2str(names[i]).equals(name)) {
        return i + 1;
      }
    }
    return UNKNOWN;
  }

  public String getKey() {
    return Util.byte2str(Util.toBase64(key, 0, key.length, true));
  }

  public String getFingerPrint(JSch jsch) {
    HASH hash = null;
    try {
      String _c = JSch.getConfig("FingerprintHash").toLowerCase(Locale.ROOT);
      Class<? extends HASH> c = Class.forName(JSch.getConfig(_c)).asSubclass(HASH.class);
      hash = c.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      if (jsch.getInstanceLogger().isEnabled(Logger.ERROR)) {
        jsch.getInstanceLogger().log(Logger.ERROR, "getFingerPrint: " + e.getMessage(), e);
      }
    }
    return Util.getFingerPrint(hash, key, true, false);
  }

  public String getComment() {
    return comment;
  }

  public String getMarker() {
    return marker;
  }

  boolean isMatched(String _host) {
    return isIncluded(_host);
  }

  private boolean isIncluded(String _host) {
    int i = 0;
    String hosts = this.host;
    int hostslen = hosts.length();
    int hostlen = _host.length();
    int j;
    while (i < hostslen) {
      j = hosts.indexOf(',', i);
      if (j == -1) {
        if (hostlen != hostslen - i)
          return false;
        return hosts.regionMatches(true, i, _host, 0, hostlen);
      }
      if (hostlen == (j - i)) {
        if (hosts.regionMatches(true, i, _host, 0, hostlen))
          return true;
      }
      i = j + 1;
    }
    return false;
  }
}
