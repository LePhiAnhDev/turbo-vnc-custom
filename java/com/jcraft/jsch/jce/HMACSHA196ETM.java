package com.jcraft.jsch.jce;

public class HMACSHA196ETM extends HMACSHA196 {

  public HMACSHA196ETM() {
    name = "hmac-sha1-96-etm@openssh.com";
    etm = true;
  }
}
