package com.jcraft.jsch.jce;

public class SignatureRSASHA224SSHCOM extends SignatureRSAN {
  @Override
  String getName() {
    return "ssh-rsa-sha224@ssh.com";
  }
}
