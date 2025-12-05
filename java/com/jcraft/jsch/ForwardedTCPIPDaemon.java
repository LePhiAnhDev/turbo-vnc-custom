package com.jcraft.jsch;

import java.io.InputStream;
import java.io.OutputStream;

public interface ForwardedTCPIPDaemon extends Runnable {
  void setChannel(ChannelForwardedTCPIP channel, InputStream in, OutputStream out);

  void setArg(Object[] arg);
}
