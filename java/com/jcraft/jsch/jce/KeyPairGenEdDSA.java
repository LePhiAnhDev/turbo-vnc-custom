package com.jcraft.jsch.jce;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.EdECPoint;
import java.util.Arrays;

public class KeyPairGenEdDSA implements com.jcraft.jsch.KeyPairGenEdDSA {
  byte[] prv; // private
  byte[] pub; // public
  int keylen;

  @Override
  public void init(String name, int keylen) throws Exception {
    this.keylen = keylen;

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(name);
    KeyPair pair = keyGen.generateKeyPair();

    EdECPublicKey pubKey = (EdECPublicKey) pair.getPublic();
    EdECPrivateKey prvKey = (EdECPrivateKey) pair.getPrivate();
    EdECPoint point = pubKey.getPoint();

    prv = prvKey.getBytes().get();
    pub = rotate(point.getY().toByteArray());
    if (point.isXOdd()) {
      pub[pub.length - 1] |= (byte) 0x80;
    }
  }

  @Override
  public byte[] getPrv() {
    return prv;
  }

  @Override
  public byte[] getPub() {
    return pub;
  }

  private byte[] rotate(byte[] in) {
    int len = in.length;
    byte[] out = new byte[len];

    for (int i = 0; i < len; i++) {
      out[i] = in[len - i - 1];
    }

    return Arrays.copyOf(out, keylen);
  }
}
