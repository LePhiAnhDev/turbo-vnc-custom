package com.jcraft.jsch.jce;

// This MAC appears to use a shortened keysize of 16 bytes instead of 32 bytes.
// See discussion at https://github.com/ronf/asyncssh/issues/399.
public class HMACSHA256SSHCOM extends HMAC {
  public HMACSHA256SSHCOM() {
    name = "hmac-sha256@ssh.com";
    bsize = 16;
    algorithm = "HmacSHA256";
  }

  @Override
  public int getBlockSize() {
    return 32;
  };
}
