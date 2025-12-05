package com.jcraft.jsch.jce;

public class SignatureRSASHA512SSHCOM extends SignatureRSAN {
  @Override
  String getName() {
    return "ssh-rsa-sha512@ssh.com";
  }
}
