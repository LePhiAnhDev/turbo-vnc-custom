package com.jcraft.jsch.jce;

public class PBKDF2HMACSHA224 extends PBKDF2 {
  @Override
  String getName() {
    return "PBKDF2WithHmacSHA224";
  }
}
