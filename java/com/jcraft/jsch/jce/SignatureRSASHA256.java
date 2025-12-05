package com.jcraft.jsch.jce;

public class SignatureRSASHA256 extends SignatureRSAN {
  @Override
  String getName() {
    return "rsa-sha2-256";
  }
}
