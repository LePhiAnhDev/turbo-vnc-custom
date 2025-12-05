package com.jcraft.jsch;

class DH25519 extends DHXEC {
  public DH25519() {
    sha_name = "sha-256";
    curve_name = "X25519";
    key_len = 32;
  }
}
