package com.jcraft.jsch.jce;

public class PBKDF2HMACSHA1 extends PBKDF2 {
  @Override
  String getName() {
    return "PBKDF2WithHmacSHA1";
  }
}
