package com.jcraft.jsch.jce;

import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

abstract class PBKDF2 implements com.jcraft.jsch.PBKDF2 {
  private SecretKeyFactory skf;
  private byte[] salt;
  private int iterations;

  abstract String getName();

  @Override
  public void init(byte[] salt, int iterations) throws Exception {
    skf = SecretKeyFactory.getInstance(getName());
    this.salt = salt;
    this.iterations = iterations;
  }

  @Override
  public byte[] getKey(byte[] _pass, int size) {
    char[] pass = new char[_pass.length];
    for (int i = 0; i < _pass.length; i++) {
      pass[i] = (char) (_pass[i] & 0xff);
    }
    try {
      PBEKeySpec spec = new PBEKeySpec(pass, salt, iterations, size * 8);
      byte[] key = skf.generateSecret(spec).getEncoded();
      return key;
    } catch (InvalidKeySpecException e) {
    }
    return null;
  }
}
