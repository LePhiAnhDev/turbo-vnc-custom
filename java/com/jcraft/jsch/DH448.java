package com.jcraft.jsch;

class DH448 extends DHXEC {
  public DH448() {
    sha_name = "sha-512";
    curve_name = "X448";
    key_len = 56;
  }
}
