package com.jcraft.jsch;

import java.util.Vector;

public class AgentIdentityRepository implements IdentityRepository {

  private AgentProxy agent;

  public AgentIdentityRepository(AgentConnector connector) {
    this.agent = new AgentProxy(connector);
  }

  @Override
  public Vector<Identity> getIdentities() {
    return agent.getIdentities();
  }

  @Override
  public boolean add(byte[] identity) {
    return agent.addIdentity(identity);
  }

  @Override
  public boolean remove(byte[] blob) {
    return agent.removeIdentity(blob);
  }

  @Override
  public void removeAll() {
    agent.removeAllIdentities();
  }

  @Override
  public String getName() {
    return agent.getConnector().getName();
  }

  @Override
  public int getStatus() {
    if (agent.getConnector().isAvailable()) {
      return RUNNING;
    } else {
      return NOTRUNNING;
    }
  }
}
