package com.jcraft.jsch.jce;

public class HMACSHA512ETM extends HMACSHA512 {
  public HMACSHA512ETM() {
    name = "hmac-sha2-512-etm@openssh.com";
    etm = true;
  }
}
