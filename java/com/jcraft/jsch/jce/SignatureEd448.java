package com.jcraft.jsch.jce;

public class SignatureEd448 extends SignatureEdDSA {
  @Override
  String getName() {
    return "ssh-ed448";
  }

  @Override
  String getAlgo() {
    return "Ed448";
  }

  @Override
  int getKeylen() {
    return 57;
  }
}
