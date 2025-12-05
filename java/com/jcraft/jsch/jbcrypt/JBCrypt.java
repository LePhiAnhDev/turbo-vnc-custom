package com.jcraft.jsch.jbcrypt;

public class JBCrypt implements com.jcraft.jsch.BCrypt {
  private BCrypt bcrypt;
  private byte[] salt;
  private int iteration;

  @Override
  public void init(byte[] salt, int iteration) throws Exception {
    bcrypt = new BCrypt();
    this.salt = salt;
    this.iteration = iteration;
  }

  @Override
  public byte[] getKey(byte[] pass, int size) {
    byte[] key = new byte[size];
    bcrypt.pbkdf(pass, salt, iteration, key);
    return key;
  }
}
