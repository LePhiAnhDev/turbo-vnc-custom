package com.jcraft.jsch.jce;

import com.jcraft.jsch.JSchException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

public class DH implements com.jcraft.jsch.DH {
  BigInteger p;
  BigInteger g;
  BigInteger e; // my public key
  byte[] e_array;
  BigInteger f; // your public key

  private KeyPairGenerator myKpairGen;
  private KeyAgreement myKeyAgree;

  @Override
  public void init() throws Exception {
    myKpairGen = KeyPairGenerator.getInstance("DH");
    myKeyAgree = KeyAgreement.getInstance("DH");
  }

  @Override
  public byte[] getE() throws Exception {
    if (e == null) {
      DHParameterSpec dhSkipParamSpec = new DHParameterSpec(p, g);
      myKpairGen.initialize(dhSkipParamSpec);
      KeyPair myKpair = myKpairGen.generateKeyPair();
      myKeyAgree.init(myKpair.getPrivate());
      e = ((DHPublicKey) (myKpair.getPublic())).getY();
      e_array = e.toByteArray();
    }
    return e_array;
  }

  @Override
  public byte[] getK() throws Exception {
    KeyFactory myKeyFac = KeyFactory.getInstance("DH");
    DHPublicKeySpec keySpec = new DHPublicKeySpec(f, p, g);
    PublicKey yourPubKey = myKeyFac.generatePublic(keySpec);
    myKeyAgree.doPhase(yourPubKey, true);
    return myKeyAgree.generateSecret();
  }

  @Override
  public void setP(byte[] p) {
    setP(new BigInteger(1, p));
  }

  @Override
  public void setG(byte[] g) {
    setG(new BigInteger(1, g));
  }

  @Override
  public void setF(byte[] f) {
    setF(new BigInteger(1, f));
  }

  void setP(BigInteger p) {
    this.p = p;
  }

  void setG(BigInteger g) {
    this.g = g;
  }

  void setF(BigInteger f) {
    this.f = f;
  }

  // e, f must be in [1, p-1].
  @Override
  public void checkRange() throws Exception {
    /*
    checkRange(e);
    checkRange(f);
    */
  }

  private void checkRange(BigInteger tmp) throws Exception {
    BigInteger one = BigInteger.ONE;
    BigInteger p_1 = p.subtract(one);
    // !(1<tmp && tmp<p-1) We expect tmp is in the range [2, p-2].
    if (!(one.compareTo(tmp) < 0 && tmp.compareTo(p_1) < 0)) {
      throw new JSchException("invalid DH value");
    }
  }
}
