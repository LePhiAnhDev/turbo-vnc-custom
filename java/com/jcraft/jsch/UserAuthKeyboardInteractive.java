package com.jcraft.jsch;

import java.util.Locale;

class UserAuthKeyboardInteractive extends UserAuth {
  @Override
  public boolean start(Session session) throws Exception {
    super.start(session);

    if (userinfo != null && !(userinfo instanceof UIKeyboardInteractive)) {
      return false;
    }

    String dest = username + "@" + session.host;
    if (session.port != 22) {
      dest += (":" + session.port);
    }
    byte[] password = session.password;

    boolean cancel = false;

    byte[] _username = Util.str2byte(username);

    while (true) {

      if (session.auth_failures >= session.max_auth_tries) {
        return false;
      }

      packet.reset();
      buf.putByte((byte) SSH_MSG_USERAUTH_REQUEST);
      buf.putString(_username);
      buf.putString(Util.str2byte("ssh-connection"));
      buf.putString(Util.str2byte("keyboard-interactive"));
      buf.putString(Util.empty);
      buf.putString(Util.empty);
      session.write(packet);

      boolean firsttime = true;
      loop:
      while (true) {
        buf = session.read(buf);
        int command = buf.getCommand() & 0xff;

        if (command == SSH_MSG_USERAUTH_SUCCESS) {
          return true;
        }
        if (command == SSH_MSG_USERAUTH_BANNER) {
          buf.getInt();
          buf.getByte();
          buf.getByte();
          byte[] _message = buf.getString();
          byte[] lang = buf.getString();
          String message = Util.byte2str(_message);
          if (userinfo != null) {
            userinfo.showMessage(message);
          }
          continue loop;
        }
        if (command == SSH_MSG_USERAUTH_FAILURE) {
          buf.getInt();
          buf.getByte();
          buf.getByte();
          byte[] foo = buf.getString();
          int partial_success = buf.getByte();

          if (partial_success != 0) {
            throw new JSchPartialAuthException(Util.byte2str(foo));
          }

          if (firsttime) {
            return false;
          }
          session.auth_failures++;
          break;
        }
        if (command == SSH_MSG_USERAUTH_INFO_REQUEST) {
          firsttime = false;
          buf.getInt();
          buf.getByte();
          buf.getByte();
          String name = Util.byte2str(buf.getString());
          String instruction = Util.byte2str(buf.getString());
          String languate_tag = Util.byte2str(buf.getString());
          int num = buf.getInt();
          String[] prompt = new String[num];
          boolean[] echo = new boolean[num];
          for (int i = 0; i < num; i++) {
            prompt[i] = Util.byte2str(buf.getString());
            echo[i] = (buf.getByte() != 0);
          }

          byte[][] response = null;

          if (password != null && prompt.length == 1 && !echo[0]
              && prompt[0].toLowerCase(Locale.ROOT).indexOf("password:") >= 0) {
            response = new byte[1][];
            response[0] = password;
            password = null;
          } else if (num > 0 || (name.length() > 0 || instruction.length() > 0)) {
            if (userinfo != null) {
              UIKeyboardInteractive kbi = (UIKeyboardInteractive) userinfo;
              String[] _response =
                  kbi.promptKeyboardInteractive(dest, name, instruction, prompt, echo);
              if (_response != null) {
                response = new byte[_response.length][];
                for (int i = 0; i < _response.length; i++) {
                  response[i] =
                      _response[i] != null ? Util.str2byte(_response[i]) : Util.empty;
                }
              }
            }
          }

          packet.reset();
          buf.putByte((byte) SSH_MSG_USERAUTH_INFO_RESPONSE);
          if (num > 0 && (response == null || num != response.length)) {

            if (response == null) {
              buf.putInt(num);
              for (int i = 0; i < num; i++) {
                buf.putString(Util.empty);
              }
            } else {
              buf.putInt(0);
            }

            if (response == null)
              cancel = true;
          } else {
            buf.putInt(num);
            for (int i = 0; i < num; i++) {
              buf.putString(response[i]);
            }
          }
          session.write(packet);
          continue loop;
        }
        return false;
      }
      if (cancel) {
        throw new JSchAuthCancelException("keyboard-interactive");
      }
    }
  }
}
