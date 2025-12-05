package com.jcraft.jsch.jce;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;

public class XDH implements com.jcraft.jsch.XDH {
  byte[] Q_array;
  XECPublicKey publicKey;
  int keylen;

  private KeyAgreement myKeyAgree;

  @Override
  public void init(String name, int keylen) throws Exception {
    this.keylen = keylen;
    myKeyAgree = KeyAgreement.getInstance("XDH");
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
    NamedParameterSpec paramSpec = new NamedParameterSpec(name);
    kpg.initialize(paramSpec);
    KeyPair kp = kpg.genKeyPair();
    publicKey = (XECPublicKey) kp.getPublic();
    Q_array = rotate(publicKey.getU().toByteArray());
    myKeyAgree.init(kp.getPrivate());
  }

  @Override
  public byte[] getQ() throws Exception {
    return Q_array;
  }

  @Override
  public byte[] getSecret(byte[] Q) throws Exception {
    Q = rotate(Q);
    byte[] u = new byte[keylen + 1];
    System.arraycopy(Q, 0, u, 1, keylen);
    XECPublicKeySpec spec = new XECPublicKeySpec(publicKey.getParams(), new BigInteger(u));
    KeyFactory kf = KeyFactory.getInstance("XDH");
    PublicKey theirPublicKey = kf.generatePublic(spec);
    myKeyAgree.doPhase(theirPublicKey, true);
    return myKeyAgree.generateSecret();
  }

  @Override
  public boolean validate(byte[] u) throws Exception {
    return u.length == keylen;
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
