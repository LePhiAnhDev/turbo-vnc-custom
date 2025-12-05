package com.jcraft.jsch.jce;

public class SignatureECDSA521 extends SignatureECDSAN {
  @Override
  String getName() {
    return "ecdsa-sha2-nistp521";
  }
}
