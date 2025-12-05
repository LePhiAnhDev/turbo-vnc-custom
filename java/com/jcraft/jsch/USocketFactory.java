package com.jcraft.jsch;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public interface USocketFactory {
  SocketChannel connect(Path path) throws IOException;

  ServerSocketChannel bind(Path path) throws IOException;
}
