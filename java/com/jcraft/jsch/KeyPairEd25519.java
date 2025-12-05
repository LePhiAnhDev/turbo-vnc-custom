package com.jcraft.jsch;

import java.util.Arrays;

class KeyPairEd25519 extends KeyPairEdDSA {

  private static int keySize = 32;

  KeyPairEd25519(JSch.InstanceLogger instLogger) {
    this(instLogger, null, null);
  }

  KeyPairEd25519(JSch.InstanceLogger instLogger, byte[] pub_array, byte[] prv_array) {
    super(instLogger, pub_array, prv_array);
  }

  @Override
  public int getKeyType() {
    return ED25519;
  }

  @Override
  public int getKeySize() {
    return keySize;
  }

  @Override
  String getSshName() {
    return "ssh-ed25519";
  }

  @Override
  String getJceName() {
    return "Ed25519";
  }

  static KeyPair fromSSHAgent(JSch.InstanceLogger instLogger, Buffer buf) throws JSchException {

    byte[][] tmp = buf.getBytes(4, "invalid key format");

    byte[] pub_array = tmp[1];
    byte[] prv_array = Arrays.copyOf(tmp[2], keySize);
    KeyPairEd25519 kpair = new KeyPairEd25519(instLogger, pub_array, prv_array);
    kpair.publicKeyComment = Util.byte2str(tmp[3]);
    kpair.vendor = VENDOR_OPENSSH;
    return kpair;
  }
}
