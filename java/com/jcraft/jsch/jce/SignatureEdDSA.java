package com.jcraft.jsch.jce;

import com.jcraft.jsch.Buffer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

abstract class SignatureEdDSA implements com.jcraft.jsch.SignatureEdDSA {

  Signature signature;
  KeyFactory keyFactory;

  abstract String getName();

  abstract String getAlgo();

  abstract int getKeylen();

  @Override
  public void init() throws Exception {
    signature = Signature.getInstance("EdDSA");
    keyFactory = KeyFactory.getInstance("EdDSA");
  }

  @Override
  public void setPubKey(byte[] y_arr) throws Exception {
    y_arr = rotate(y_arr);
    boolean xOdd = (y_arr[0] & 0x80) != 0;
    y_arr[0] &= 0x7f;
    BigInteger y = new BigInteger(y_arr);

    NamedParameterSpec paramSpec = new NamedParameterSpec(getAlgo());
    EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, new EdECPoint(xOdd, y));
    PublicKey pubKey = keyFactory.generatePublic(pubSpec);
    signature.initVerify(pubKey);
  }

  @Override
  public void setPrvKey(byte[] bytes) throws Exception {
    NamedParameterSpec paramSpec = new NamedParameterSpec(getAlgo());
    EdECPrivateKeySpec privSpec = new EdECPrivateKeySpec(paramSpec, bytes);
    PrivateKey prvKey = keyFactory.generatePrivate(privSpec);
    signature.initSign(prvKey);
  }

  @Override
  public byte[] sign() throws Exception {
    byte[] sig = signature.sign();
    return sig;
  }

  @Override
  public void update(byte[] foo) throws Exception {
    signature.update(foo);
  }

  @Override
  public boolean verify(byte[] sig) throws Exception {
    int i = 0;
    int j = 0;
    byte[] tmp;
    Buffer buf = new Buffer(sig);

    String foo = new String(buf.getString(), StandardCharsets.UTF_8);
    if (foo.equals(getName())) {
      j = buf.getInt();
      i = buf.getOffSet();
      tmp = new byte[j];
      System.arraycopy(sig, i, tmp, 0, j);
      sig = tmp;
    }

    return signature.verify(sig);
  }

  private byte[] rotate(byte[] in) {
    int len = in.length;
    byte[] out = new byte[len];

    for (int i = 0; i < len; i++) {
      out[i] = in[len - i - 1];
    }

    return Arrays.copyOf(out, getKeylen());
  }
}
