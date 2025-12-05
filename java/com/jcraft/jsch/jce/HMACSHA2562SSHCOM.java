package com.jcraft.jsch.jce;

public class HMACSHA2562SSHCOM extends HMAC {
  public HMACSHA2562SSHCOM() {
    name = "hmac-sha256-2@ssh.com";
    bsize = 32;
    algorithm = "HmacSHA256";
  }
}
