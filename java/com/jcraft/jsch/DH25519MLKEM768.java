package com.jcraft.jsch;

class DH25519MLKEM768 extends DHXECKEM {
  public DH25519MLKEM768() {
    kem_name = "mlkem768";
    sha_name = "sha-256";
    curve_name = "X25519";
    kem_pubkey_len = 1184;
    kem_encap_len = 1088;
    xec_key_len = 32;
  }
}
