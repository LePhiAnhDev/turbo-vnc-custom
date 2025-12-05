package com.jcraft.jsch.jce;

public class PBKDF2HMACSHA512256 extends PBKDF2 {
  @Override
  String getName() {
    return "PBKDF2WithHmacSHA512/256";
  }
}
