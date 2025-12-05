package com.jcraft.jsch.jce;

public class SignatureECDSA256 extends SignatureECDSAN {
  @Override
  String getName() {
    return "ecdsa-sha2-nistp256";
  }
}
