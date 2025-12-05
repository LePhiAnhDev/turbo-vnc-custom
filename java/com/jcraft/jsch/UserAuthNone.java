package com.jcraft.jsch;

public class UserAuthNone extends UserAuth {
  protected static final int SSH_MSG_SERVICE_REQUEST = 5;
  protected static final int SSH_MSG_SERVICE_ACCEPT = 6;

  private String methods;

  @Override
  public boolean start(Session session) throws Exception {
    super.start(session);

    packet.reset();
    buf.putByte((byte) SSH_MSG_SERVICE_REQUEST);
    buf.putString(Util.str2byte("ssh-userauth"));
    session.write(packet);

    if (session.getLogger().isEnabled(Logger.INFO)) {
      session.getLogger().log(Logger.INFO, "SSH_MSG_SERVICE_REQUEST sent");
    }

    buf = session.read(buf);
    int command = buf.getCommand();

    boolean result = (command == SSH_MSG_SERVICE_ACCEPT);

    if (session.getLogger().isEnabled(Logger.INFO)) {
      session.getLogger().log(Logger.INFO, "SSH_MSG_SERVICE_ACCEPT received");
    }
    if (!result)
      return false;

    if (!session.getConfig("enable_auth_none").equals("yes"))
      return false;

    byte[] _username = Util.str2byte(username);

    packet.reset();
    buf.putByte((byte) UserAuth.SSH_MSG_USERAUTH_REQUEST);
    buf.putString(_username);
    buf.putString(Util.str2byte("ssh-connection"));
    buf.putString(Util.str2byte("none"));
    session.write(packet);

    loop:
    while (true) {
      buf = session.read(buf);
      command = buf.getCommand() & 0xff;

      if (command == UserAuth.SSH_MSG_USERAUTH_SUCCESS) {
        return true;
      }
      if (command == UserAuth.SSH_MSG_USERAUTH_BANNER) {
        buf.getInt();
        buf.getByte();
        buf.getByte();
        byte[] _message = buf.getString();
        byte[] lang = buf.getString();
        String message = Util.byte2str(_message);
        if (userinfo != null) {
          try {
            userinfo.showMessage(message);
          } catch (RuntimeException ee) {
          }
        }
        continue loop;
      }
      if (command == UserAuth.SSH_MSG_USERAUTH_FAILURE) {
        buf.getInt();
        buf.getByte();
        buf.getByte();
        byte[] foo = buf.getString();
        int partial_success = buf.getByte();
        setMethods(Util.byte2str(foo));
        break;
      } else {
        throw new JSchException("USERAUTH fail (" + command + ")");
      }
    }
    return false;
  }

  protected String getMethods() {
    return methods;
  }

  protected void setMethods(String methods) {
    this.methods = methods;
  }
}
