package com.jcraft.jsch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SSHAgentConnector implements AgentConnector {
  private static final int MAX_AGENT_REPLY_LEN = 256 * 1024;

  private USocketFactory factory;
  private Path usocketPath;

  public SSHAgentConnector() throws AgentProxyException {
    this(getUSocketFactory(), getSshAuthSocket());
  }

  public SSHAgentConnector(Path usocketPath) throws AgentProxyException {
    this(getUSocketFactory(), usocketPath);
  }

  public SSHAgentConnector(USocketFactory factory) throws AgentProxyException {
    this(factory, getSshAuthSocket());
  }

  public SSHAgentConnector(USocketFactory factory, Path usocketPath) {
    this.factory = factory;
    this.usocketPath = usocketPath;
  }

  @Override
  public String getName() {
    return "ssh-agent";
  }

  @Override
  @SuppressWarnings("try")
  public boolean isAvailable() {
    try (SocketChannel foo = open()) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private SocketChannel open() throws IOException {
    return factory.connect(usocketPath);
  }

  @Override
  public void query(Buffer buffer) throws AgentProxyException {
    try (SocketChannel sock = open()) {
      writeFull(sock, buffer, 0, buffer.getLength());
      buffer.rewind();
      int i = readFull(sock, buffer, 0, 4); // length
      i = buffer.getInt();
      if (i <= 0 || i > MAX_AGENT_REPLY_LEN) {
        throw new AgentProxyException("Illegal length: " + i);
      }
      buffer.rewind();
      buffer.checkFreeSize(i);
      i = readFull(sock, buffer, 0, i);
    } catch (IOException e) {
      throw new AgentProxyException(e.toString(), e);
    }
  }

  private static USocketFactory getUSocketFactory() throws AgentProxyException {
    return new UnixDomainSocketFactory();
  }

  private static Path getSshAuthSocket() throws AgentProxyException {
    String ssh_auth_sock = Util.getSystemEnv("SSH_AUTH_SOCK");
    if (ssh_auth_sock == null) {
      throw new AgentProxyException("SSH_AUTH_SOCK is not defined.");
    }
    return Paths.get(ssh_auth_sock);
  }

  private static int readFull(SocketChannel sock, Buffer buffer, int s, int len)
      throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(buffer.buffer, s, len);
    int _len = len;
    while (len > 0) {
      int j = sock.read(bb);
      if (j < 0)
        return -1;
      if (j > 0) {
        len -= j;
      }
    }
    return _len;
  }

  private static int writeFull(SocketChannel sock, Buffer buffer, int s, int len)
      throws IOException {
    ByteBuffer bb = ByteBuffer.wrap(buffer.buffer, s, len);
    int _len = len;
    while (len > 0) {
      int j = sock.write(bb);
      if (j < 0)
        return -1;
      if (j > 0) {
        len -= j;
      }
    }
    return _len;
  }
}
