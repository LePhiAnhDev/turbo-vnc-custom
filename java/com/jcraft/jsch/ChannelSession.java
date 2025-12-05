package com.jcraft.jsch;

import java.util.Enumeration;
import java.util.Hashtable;

class ChannelSession extends Channel {
  private static byte[] _session = Util.str2byte("session");

  protected boolean agent_forwarding = false;
  protected boolean xforwading = false;
  protected Hashtable<byte[], byte[]> env = null;

  protected boolean pty = false;

  protected String ttype = "vt100";
  protected int tcol = 80;
  protected int trow = 24;
  protected int twp = 640;
  protected int thp = 480;
  protected byte[] terminal_mode = null;

  ChannelSession() {
    super();
    type = _session;
    io = new IO();
  }

  public void setAgentForwarding(boolean enable) {
    agent_forwarding = enable;
  }

  @Override
  public void setXForwarding(boolean enable) {
    xforwading = enable;
  }

  @Deprecated
  public void setEnv(Hashtable<byte[], byte[]> env) {
    synchronized (this) {
      this.env = env;
    }
  }

  public void setEnv(String name, String value) {
    setEnv(Util.str2byte(name), Util.str2byte(value));
  }

  public void setEnv(byte[] name, byte[] value) {
    synchronized (this) {
      getEnv().put(name, value);
    }
  }

  private Hashtable<byte[], byte[]> getEnv() {
    if (env == null)
      env = new Hashtable<>();
    return env;
  }

  public void setPty(boolean enable) {
    pty = enable;
  }

  public void setTerminalMode(byte[] terminal_mode) {
    this.terminal_mode = terminal_mode;
  }

  public void setPtySize(int col, int row, int wp, int hp) {
    setPtyType(this.ttype, col, row, wp, hp);
    if (!pty || !isConnected()) {
      return;
    }
    try {
      RequestWindowChange request = new RequestWindowChange();
      request.setSize(col, row, wp, hp);
      request.request(getSession(), this);
    } catch (Exception e) {
    }
  }

  public void setPtyType(String ttype) {
    setPtyType(ttype, 80, 24, 640, 480);
  }

  public void setPtyType(String ttype, int col, int row, int wp, int hp) {
    this.ttype = ttype;
    this.tcol = col;
    this.trow = row;
    this.twp = wp;
    this.thp = hp;
  }

  protected void sendRequests() throws Exception {
    Session _session = getSession();
    Request request;
    if (agent_forwarding) {
      request = new RequestAgentForwarding();
      request.request(_session, this);
    }

    if (xforwading) {
      request = new RequestX11();
      request.request(_session, this);
    }

    if (pty) {
      request = new RequestPtyReq();
      ((RequestPtyReq) request).setTType(ttype);
      ((RequestPtyReq) request).setTSize(tcol, trow, twp, thp);
      if (terminal_mode != null) {
        ((RequestPtyReq) request).setTerminalMode(terminal_mode);
      }
      request.request(_session, this);
    }

    if (env != null) {
      for (Enumeration<byte[]> _env = env.keys(); _env.hasMoreElements();) {
        byte[] name = _env.nextElement();
        byte[] value = env.get(name);
        request = new RequestEnv();
        ((RequestEnv) request).setEnv(toByteArray(name), toByteArray(value));
        request.request(_session, this);
      }
    }
  }

  private byte[] toByteArray(Object o) {
    if (o instanceof String) {
      return Util.str2byte((String) o);
    }
    return (byte[]) o;
  }

  @Override
  void run() {
    Buffer buf = new Buffer(rmpsize);
    Packet packet = new Packet(buf);
    int i = -1;
    try {
      Session _session = getSession();
      while (isConnected() && thread != null && io != null && io.in != null) {
        i = io.in.read(buf.buffer, 14, buf.buffer.length - 14 - _session.getBufferMargin());
        if (i == 0)
          continue;
        if (i == -1) {
          eof();
          break;
        }
        if (close)
          break;
        packet.reset();
        buf.putByte((byte) Session.SSH_MSG_CHANNEL_DATA);
        buf.putInt(recipient);
        buf.putInt(i);
        buf.skip(i);
        _session.write(packet, this, i);
      }
    } catch (Exception e) {
    }
    Thread _thread = thread;
    if (_thread != null) {
      synchronized (_thread) {
        _thread.notifyAll();
      }
    }
    thread = null;
  }
}
