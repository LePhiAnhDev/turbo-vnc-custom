package com.jcraft.jsch.jce;

public class SignatureECDSA384 extends SignatureECDSAN {
  @Override
  String getName() {
    return "ecdsa-sha2-nistp384";
  }
}
