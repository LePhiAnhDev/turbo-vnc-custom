package com.jcraft.jsch.jce;

public class HMACMD5ETM extends HMACMD5 {
  public HMACMD5ETM() {
    name = "hmac-md5-etm@openssh.com";
    etm = true;
  }
}
