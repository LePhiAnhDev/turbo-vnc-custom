package com.jcraft.jsch.jce;

public class HMACSHA256ETM extends HMACSHA256 {
  public HMACSHA256ETM() {
    name = "hmac-sha2-256-etm@openssh.com";
    etm = true;
  }
}
