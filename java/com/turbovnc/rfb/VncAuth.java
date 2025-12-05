package com.turbovnc.rfb;

public class VncAuth {

  public static final int OK = 0;
  public static final int FAILED = 1;
  public static final int TOO_MANY = 2;  // deprecated

  public static final int CHALLENGE_SIZE = 16;

  public static void encryptChallenge(byte[] challenge, String passwd) {
    byte[] key = new byte[8];
    for (int i = 0; i < 8 && i < passwd.length(); i++) {
      key[i] = (byte)passwd.charAt(i);
    }

    DesCipher des = new DesCipher(key);

    for (int j = 0; j < CHALLENGE_SIZE; j += 8)
      des.encrypt(challenge, j, challenge, j);
  }

  void obfuscatePasswd(String passwd, byte[] obfuscated) {
    for (int i = 0; i < 8; i++) {
      if (i < passwd.length())
        obfuscated[i] = (byte)passwd.charAt(i);
      else
        obfuscated[i] = 0;
    }
    DesCipher des = new DesCipher(obfuscationKey);
    des.encrypt(obfuscated, 0, obfuscated, 0);
  }

  public static String unobfuscatePasswd(byte[] obfuscated) {
    DesCipher des = new DesCipher(obfuscationKey);
    des.decrypt(obfuscated, 0, obfuscated, 0);
    int len;
    for (len = 0; len < 8; len++) {
      if (obfuscated[len] == 0) break;
    }
    char[] plain = new char[len];
    for (int i = 0; i < len; i++) {
      plain[i] = (char)obfuscated[i];
    }
    return new String(plain);
  }

  static byte[] obfuscationKey = { 23, 82, 107, 6, 35, 78, 88, 7 };
}
