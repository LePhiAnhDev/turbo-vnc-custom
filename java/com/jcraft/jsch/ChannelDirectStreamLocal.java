package com.jcraft.jsch;

import static com.jcraft.jsch.Session.SSH_MSG_CHANNEL_OPEN;

public class ChannelDirectStreamLocal extends ChannelDirectTCPIP {

  private static final int LOCAL_WINDOW_SIZE_MAX = 0x20000;
  private static final int LOCAL_MAXIMUM_PACKET_SIZE = 0x4000;
  private static final byte[] _type = Util.str2byte("direct-streamlocal@openssh.com");

  private String socketPath;

  ChannelDirectStreamLocal() {
    super();
    type = _type;
    lwsize_max = LOCAL_WINDOW_SIZE_MAX;
    lwsize = LOCAL_WINDOW_SIZE_MAX;
    lmpsize = LOCAL_MAXIMUM_PACKET_SIZE;
  }

  @Override
  protected Packet genChannelOpenPacket() {
    if (socketPath == null) {
      session.getLogger().log(Logger.FATAL, "socketPath must be set");
      throw new RuntimeException("socketPath must be set");
    }

    Buffer buf = new Buffer(50 + socketPath.length() + session.getBufferMargin());
    Packet packet = new Packet(buf);
    packet.reset();
    buf.putByte((byte) SSH_MSG_CHANNEL_OPEN);
    buf.putString(this.type);
    buf.putInt(id);
    buf.putInt(lwsize);
    buf.putInt(lmpsize);
    buf.putString(Util.str2byte(socketPath));
    buf.putString(Util.str2byte(originator_IP_address));
    buf.putInt(originator_port);
    return packet;
  }

  public String getSocketPath() {
    return socketPath;
  }

  public void setSocketPath(String socketPath) {
    this.socketPath = socketPath;
  }
}
