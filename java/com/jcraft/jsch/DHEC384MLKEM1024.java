package com.jcraft.jsch;

class DHEC384MLKEM1024 extends DHECNKEM {
  public DHEC384MLKEM1024() {
    kem_name = "mlkem1024";
    sha_name = "sha-384";
    kem_pubkey_len = 1568;
    kem_encap_len = 1568;
    ecdh_key_size = 384;
    ecdh_key_len = 97;
  }
}
