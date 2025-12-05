package com.jcraft.jsch;

public interface SignatureDSA extends Signature {
  void setPubKey(byte[] y, byte[] p, byte[] q, byte[] g) throws Exception;

  void setPrvKey(byte[] x, byte[] p, byte[] q, byte[] g) throws Exception;
}
