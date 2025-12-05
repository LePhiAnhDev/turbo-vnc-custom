package com.jcraft.jsch;

public interface DH {
  void init() throws Exception;

  void setP(byte[] p);

  void setG(byte[] g);

  byte[] getE() throws Exception;

  void setF(byte[] f);

  byte[] getK() throws Exception;

  void checkRange() throws Exception;
}
