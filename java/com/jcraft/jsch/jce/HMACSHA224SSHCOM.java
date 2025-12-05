package com.jcraft.jsch.jce;

public class HMACSHA224SSHCOM extends HMAC {
  public HMACSHA224SSHCOM() {
    name = "hmac-sha224@ssh.com";
    bsize = 28;
    algorithm = "HmacSHA224";
  }
}
