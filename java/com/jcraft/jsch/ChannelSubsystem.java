package com.jcraft.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChannelSubsystem extends ChannelSession {
  boolean want_reply = true;
  String subsystem = "";

  public void setWantReply(boolean foo) {
    want_reply = foo;
  }

  public void setSubsystem(String foo) {
    subsystem = foo;
  }

  @Override
  public void start() throws JSchException {
    Session _session = getSession();
    try {
      Request request;
      if (xforwading) {
        request = new RequestX11();
        request.request(_session, this);
      }
      if (pty) {
        request = new RequestPtyReq();
        request.request(_session, this);
      }
      request = new RequestSubsystem();
      ((RequestSubsystem) request).request(_session, this, subsystem, want_reply);
    } catch (Exception e) {
      if (e instanceof JSchException) {
        throw (JSchException) e;
      }
      throw new JSchException("ChannelSubsystem", e);
    }
    if (io.in != null) {
      thread = new Thread(this::run);
      thread.setName("Subsystem for " + _session.host);
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

  public void setErrStream(OutputStream out) {
    setExtOutputStream(out);
  }

  public InputStream getErrStream() throws IOException {
    return getExtInputStream();
  }
}
