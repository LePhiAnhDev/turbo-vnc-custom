package com.jcraft.jsch;

public interface Identity {

  public boolean setPassphrase(byte[] passphrase) throws JSchException;

  public byte[] getPublicKeyBlob();

  public String getFingerPrint();

  public byte[] getSignature(byte[] data);

  public default byte[] getSignature(byte[] data, String alg) {
    return getSignature(data);
  }

  @Deprecated
  public default boolean decrypt() {
    throw new UnsupportedOperationException("not implemented");
  }

  public String getAlgName();

  public String getName();

  public boolean isEncrypted();

  public void clear();
}
