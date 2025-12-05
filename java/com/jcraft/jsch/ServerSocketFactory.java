package com.jcraft.jsch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public interface ServerSocketFactory {
  public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr)
      throws IOException;
}
