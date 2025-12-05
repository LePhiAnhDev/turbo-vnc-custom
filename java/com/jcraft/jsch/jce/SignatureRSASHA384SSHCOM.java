package com.jcraft.jsch.jce;

public class SignatureRSASHA384SSHCOM extends SignatureRSAN {
  @Override
  String getName() {
    return "ssh-rsa-sha384@ssh.com";
  }
}
