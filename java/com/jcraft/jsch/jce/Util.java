package com.jcraft.jsch.jce;

class Util {
  static void bzero(byte[] foo) {
    if (foo == null)
      return;
    for (int i = 0; i < foo.length; i++)
      foo[i] = 0;
  }
}
