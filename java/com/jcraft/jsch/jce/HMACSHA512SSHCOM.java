package com.jcraft.jsch.jce;

public class HMACSHA512SSHCOM extends HMAC {
  public HMACSHA512SSHCOM() {
    name = "hmac-sha512@ssh.com";
    bsize = 64;
    algorithm = "HmacSHA512";
  }
}
