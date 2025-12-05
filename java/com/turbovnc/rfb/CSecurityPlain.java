package com.turbovnc.rfb;

import com.turbovnc.rdr.*;

public class CSecurityPlain extends CSecurity {

  public CSecurityPlain() {}

  public boolean processMsg(CConnection cc) {
    OutStream os = cc.getOutStream();

    StringBuffer username = new StringBuffer();
    StringBuffer password = new StringBuffer();

    cc.getUserPasswd(username, password);

    // Return the response to the server
    os.writeU32(username.length());
    os.writeU32(password.length());
    byte[] utf8str;
    try {
      utf8str = username.toString().getBytes("UTF8");
      os.writeBytes(utf8str, 0, username.length());
      utf8str = password.toString().getBytes("UTF8");
      os.writeBytes(utf8str, 0, password.length());
    } catch (java.io.UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    os.flush();
    return true;
  }

  public final int getType() { return RFB.SECTYPE_PLAIN; }
  public final String getDescription() { return "Plain"; }
  public final String getProtocol() { return "None"; }

  static LogWriter vlog = new LogWriter("Plain");
}
