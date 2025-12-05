package com.jcraft.jsch;

public interface AgentConnector {
  String getName();

  boolean isAvailable();

  void query(Buffer buffer) throws AgentProxyException;
}
