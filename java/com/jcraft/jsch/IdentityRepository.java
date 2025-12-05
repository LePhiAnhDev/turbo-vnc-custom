package com.jcraft.jsch;

import java.util.Vector;

public interface IdentityRepository {
  public static final int UNAVAILABLE = 0;
  public static final int NOTRUNNING = 1;
  public static final int RUNNING = 2;

  public String getName();

  public int getStatus();

  public Vector<Identity> getIdentities();

  public boolean add(byte[] identity);

  public boolean remove(byte[] blob);

  public void removeAll();
}
