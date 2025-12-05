package com.jcraft.jsch.jce;

public class SignatureRSASHA512 extends SignatureRSAN {
  @Override
  String getName() {
    return "rsa-sha2-512";
  }
}
