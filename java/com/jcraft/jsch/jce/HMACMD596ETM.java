package com.jcraft.jsch.jce;

public class HMACMD596ETM extends HMACMD596 {
  public HMACMD596ETM() {
    name = "hmac-md5-96-etm@openssh.com";
    etm = true;
  }
}
