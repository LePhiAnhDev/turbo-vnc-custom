package com.jcraft.jsch.jce;

public class SignatureRSASHA256SSHCOM extends SignatureRSAN {
  @Override
  String getName() {
    return "ssh-rsa-sha256@ssh.com";
  }
}
