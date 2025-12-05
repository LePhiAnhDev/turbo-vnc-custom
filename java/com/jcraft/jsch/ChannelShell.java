package com.jcraft.jsch;

public class ChannelShell extends ChannelSession {

  ChannelShell() {
    super();
    pty = true;
  }

  @Override
  public void start() throws JSchException {
    Session _session = getSession();
    try {
      sendRequests();

      Request request = new RequestShell();
      request.request(_session, this);
    } catch (Exception e) {
      if (e instanceof JSchException)
        throw (JSchException) e;
      throw new JSchException("ChannelShell", e);
    }

    if (io.in != null) {
      thread = new Thread(this::run);
      thread.setName("Shell for " + _session.host);
      if (_session.daemon_thread) {
        thread.setDaemon(_session.daemon_thread);
      }
      thread.start();
    }
  }

  @Override
  void init() throws JSchException {
    io.setInputStream(getSession().in);
    io.setOutputStream(getSession().out);
  }
}
