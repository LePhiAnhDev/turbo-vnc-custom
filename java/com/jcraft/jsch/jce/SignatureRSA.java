package com.jcraft.jsch.jce;

public class SignatureRSA extends SignatureRSAN {
  @Override
  String getName() {
    return "ssh-rsa";
  }
}