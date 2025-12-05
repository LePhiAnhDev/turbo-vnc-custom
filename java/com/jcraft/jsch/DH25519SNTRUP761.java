package com.jcraft.jsch;

class DH25519SNTRUP761 extends DHXECKEM {
  public DH25519SNTRUP761() {
    kem_name = "sntrup761";
    sha_name = "sha-512";
    curve_name = "X25519";
    kem_pubkey_len = 1158;
    kem_encap_len = 1039;
    xec_key_len = 32;
  }
}
