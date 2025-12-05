package com.jcraft.jsch;

import java.util.Vector;

class IdentityRepositoryWrapper implements IdentityRepository {
  private IdentityRepository ir;
  private Vector<Identity> cache = new Vector<>();
  private boolean keep_in_cache = false;

  IdentityRepositoryWrapper(IdentityRepository ir) {
    this(ir, false);
  }

  IdentityRepositoryWrapper(IdentityRepository ir, boolean keep_in_cache) {
    this.ir = ir;
    this.keep_in_cache = keep_in_cache;
  }

  @Override
  public String getName() {
    return ir.getName();
  }

  @Override
  public int getStatus() {
    return ir.getStatus();
  }

  @Override
  public boolean add(byte[] identity) {
    return ir.add(identity);
  }

  @Override
  public boolean remove(byte[] blob) {
    return ir.remove(blob);
  }

  @Override
  public void removeAll() {
    cache.removeAllElements();
    ir.removeAll();
  }

  @Override
  public Vector<Identity> getIdentities() {
    Vector<Identity> result = new Vector<>();
    for (int i = 0; i < cache.size(); i++) {
      Identity identity = cache.elementAt(i);
      result.add(identity);
    }
    Vector<Identity> tmp = ir.getIdentities();
    for (int i = 0; i < tmp.size(); i++) {
      result.add(tmp.elementAt(i));
    }
    return result;
  }

  void add(Identity identity) {
    if (!keep_in_cache && !identity.isEncrypted() && (identity instanceof IdentityFile)) {
      try {
        ir.add(((IdentityFile) identity).getKeyPair().forSSHAgent());
      } catch (JSchException e) {
        // an exception will not be thrown.
      }
    } else
      cache.addElement(identity);
  }

  void check() {
    if (cache.size() > 0) {
      Object[] identities = cache.toArray();
      for (int i = 0; i < identities.length; i++) {
        Identity identity = (Identity) (identities[i]);
        cache.removeElement(identity);
        add(identity);
      }
    }
  }
}
