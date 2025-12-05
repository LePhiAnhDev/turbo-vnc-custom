package com.jcraft.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChannelExec extends ChannelSession {

  byte[] command = new byte[0];

  @Override
  public void start() throws JSchException {
    Session _session = getSession();
    try {
      sendRequests();
      Request request = new RequestExec(command);
      request.request(_session, this);
    } catch (Exception e) {
      if (e instanceof JSchException)
        throw (JSchException) e;
      throw new JSchException("ChannelExec", e);
    }

    if (io.in != null) {
      thread = new Thread(this::run);
      thread.setName("Exec thread " + _session.getHost());
      if (_session.daemon_thread) {
        thread.setDaemon(_session.daemon_thread);
      }
      thread.start();
    }
  }

  public void setCommand(String command) {
    this.command = Util.str2byte(command);
  }

  public void setCommand(byte[] command) {
    this.command = command;
  }

  @Override
  void init() throws JSchException {
    io.setInputStream(getSession().in);
    io.setOutputStream(getSession().out);
  }

  public void setErrStream(OutputStream out) {
    setExtOutputStream(out);
  }

  public void setErrStream(OutputStream out, boolean dontclose) {
    setExtOutputStream(out, dontclose);
  }

  public InputStream getErrStream() throws IOException {
    return getExtInputStream();
  }
}
