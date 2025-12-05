package com.jcraft.jsch;

class DHEC256MLKEM768 extends DHECNKEM {
  public DHEC256MLKEM768() {
    kem_name = "mlkem768";
    sha_name = "sha-256";
    kem_pubkey_len = 1184;
    kem_encap_len = 1088;
    ecdh_key_size = 256;
    ecdh_key_len = 65;
  }
}
