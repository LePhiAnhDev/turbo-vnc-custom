package com.jcraft.jsch.jce;

public class SignatureEd25519 extends SignatureEdDSA {
  @Override
  String getName() {
    return "ssh-ed25519";
  }

  @Override
  String getAlgo() {
    return "Ed25519";
  }

  @Override
  int getKeylen() {
    return 32;
  }
}
