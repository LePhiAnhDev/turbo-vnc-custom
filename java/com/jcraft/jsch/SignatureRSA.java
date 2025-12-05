package com.jcraft.jsch;

public interface SignatureRSA extends Signature {
  void setPubKey(byte[] e, byte[] n) throws Exception;

  void setPrvKey(byte[] d, byte[] n) throws Exception;
}
