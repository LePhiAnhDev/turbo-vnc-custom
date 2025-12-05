package com.jcraft.jsch;

public interface SignatureEdDSA extends Signature {
  void setPubKey(byte[] y_arr) throws Exception;

  void setPrvKey(byte[] bytes) throws Exception;
}
