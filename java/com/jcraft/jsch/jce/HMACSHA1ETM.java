package com.jcraft.jsch.jce;

public class HMACSHA1ETM extends HMACSHA1 {
  public HMACSHA1ETM() {
    name = "hmac-sha1-etm@openssh.com";
    etm = true;
  }
}
