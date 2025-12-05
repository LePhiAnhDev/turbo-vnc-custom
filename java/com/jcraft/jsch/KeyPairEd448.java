package com.jcraft.jsch;

import java.util.Arrays;

class KeyPairEd448 extends KeyPairEdDSA {

  private static int keySize = 57;

  KeyPairEd448(JSch.InstanceLogger instLogger) {
    this(instLogger, null, null);
  }

  KeyPairEd448(JSch.InstanceLogger instLogger, byte[] pub_array, byte[] prv_array) {
    super(instLogger, pub_array, prv_array);
  }

  @Override
  public int getKeyType() {
    return ED448;
  }

  @Override
  public int getKeySize() {
    return keySize;
  }

  @Override
  String getSshName() {
    return "ssh-ed448";
  }

  @Override
  String getJceName() {
    return "Ed448";
  }

  static KeyPair fromSSHAgent(JSch.InstanceLogger instLogger, Buffer buf) throws JSchException {

    byte[][] tmp = buf.getBytes(4, "invalid key format");

    byte[] pub_array = tmp[1];
    byte[] prv_array = Arrays.copyOf(tmp[2], keySize);
    KeyPairEd448 kpair = new KeyPairEd448(instLogger, pub_array, prv_array);
    kpair.publicKeyComment = Util.byte2str(tmp[3]);
    kpair.vendor = VENDOR_OPENSSH;
    return kpair;
  }
}
