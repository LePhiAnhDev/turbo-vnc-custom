package com.jcraft.jsch;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public class UnixDomainSocketFactory implements USocketFactory {

  public UnixDomainSocketFactory() throws AgentProxyException {}

  @Override
  public SocketChannel connect(Path path) throws IOException {
    UnixDomainSocketAddress sockAddr = UnixDomainSocketAddress.of(path);
    SocketChannel sock = SocketChannel.open(StandardProtocolFamily.UNIX);
    sock.configureBlocking(true);
    sock.connect(sockAddr);
    return sock;
  }

  @Override
  public ServerSocketChannel bind(Path path) throws IOException {
    UnixDomainSocketAddress sockAddr = UnixDomainSocketAddress.of(path);
    ServerSocketChannel sock = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
    sock.configureBlocking(true);
    sock.bind(sockAddr);
    return sock;
  }
}
