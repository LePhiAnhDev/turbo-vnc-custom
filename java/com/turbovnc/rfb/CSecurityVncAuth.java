package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public class CSecurityVncAuth extends CSecurity {

  public CSecurityVncAuth() {}

  private static final int VNC_AUTH_CHALLENGE_SIZE = 16;

  public boolean processMsg(CConnection cc) {
    InStream is = cc.getInStream();
    OutStream os = cc.getOutStream();

    // Read the challenge & obtain the user's password
    byte[] challenge = new byte[VNC_AUTH_CHALLENGE_SIZE];
    is.readBytes(challenge, 0, VNC_AUTH_CHALLENGE_SIZE);
    StringBuffer passwd = new StringBuffer();
    cc.getUserPasswd(null, passwd);

    // Calculate the correct response
    byte[] key = new byte[8];
    int pwdLen = passwd.length();
    byte[] utf8str = new byte[pwdLen];
    try {
      utf8str = passwd.toString().getBytes("UTF8");
    } catch (java.io.UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    for (int i = 0; i < 8; i++)
      key[i] = i < pwdLen ? utf8str[i] : 0;
    DesCipher des = new DesCipher(key);
    for (int j = 0; j < VNC_AUTH_CHALLENGE_SIZE; j += 8)
      des.encrypt(challenge, j, challenge, j);

    // Return the response to the server
    os.writeBytes(challenge, 0, VNC_AUTH_CHALLENGE_SIZE);
    os.flush();
    return true;
  }

  public final int getType() { return RFB.SECTYPE_VNCAUTH; }
  public final String getDescription() { return "VncAuth"; }
  public final String getProtocol() { return "None"; }

  static LogWriter vlog = new LogWriter("VncAuth");
}
