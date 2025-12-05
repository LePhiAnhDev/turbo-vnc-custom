package com.turbovnc.network;

public interface FileDescriptor {

  int read(byte[] buf, int bufPtr, int length);
  int write(byte[] buf, int bufPtr, int length);
  int select(int interestOps, Integer timeout);
  void close();

}
