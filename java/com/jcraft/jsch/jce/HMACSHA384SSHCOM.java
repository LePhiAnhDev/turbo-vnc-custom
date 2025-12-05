package com.jcraft.jsch.jce;

public class HMACSHA384SSHCOM extends HMAC {
  public HMACSHA384SSHCOM() {
    name = "hmac-sha384@ssh.com";
    bsize = 48;
    algorithm = "HmacSHA384";
  }
}
