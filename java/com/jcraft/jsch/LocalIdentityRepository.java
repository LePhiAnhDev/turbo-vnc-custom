package com.jcraft.jsch;

import java.util.Vector;

public class LocalIdentityRepository implements IdentityRepository {
  private static final String name = "Local Identity Repository";

  private Vector<Identity> identities = new Vector<>();
  private JSch.InstanceLogger instLogger;

  LocalIdentityRepository(JSch.InstanceLogger instLogger) {
    this.instLogger = instLogger;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getStatus() {
    return RUNNING;
  }

  @Override
  public synchronized Vector<Identity> getIdentities() {
    removeDupulicates();
    Vector<Identity> v = new Vector<>();
    for (int i = 0; i < identities.size(); i++) {
      v.addElement(identities.elementAt(i));
    }
    return v;
  }

  public synchronized void copyFrom(IdentityRepository sourceRepo) {
    Vector sourceIdentities = sourceRepo.getIdentities();
    for (int i = 0; i < sourceIdentities.size(); i++) {
      Identity sourceIdentity = (Identity)sourceIdentities.elementAt(i);
      identities.addElement(sourceIdentity);
    }
  }

  public synchronized void add(Identity identity) {
    /* If a decrypted private key already exists with the same fingerprint as
       the new private key, then promote it to the head of the chain.
       Otherwise, add the new private key to the head of the chain if it is
       decrypted or to the end of the chain if it is encrypted.  This emulates
       the behavior of OpenSSH. */
    String newFingerPrint = identity.getFingerPrint();
    if (newFingerPrint != null) {
      for (int i = 0; i < identities.size(); i++) {
        Identity oldIdentity = identities.elementAt(i);
        String oldFingerPrint = oldIdentity.getFingerPrint();
        if (oldFingerPrint != null && newFingerPrint.equals(oldFingerPrint)
            && !oldIdentity.isEncrypted()) {
          identities.removeElement(oldIdentity);
          identities.insertElementAt(oldIdentity, 0);
          return;
        }
      }
    }
    if (!identities.contains(identity)) {
      if (identity.isEncrypted())
        identities.addElement(identity);
      else
        identities.insertElementAt(identity, 0);
    }
  }

  @Override
  public synchronized boolean add(byte[] identity) {
    try {
      Identity _identity = IdentityFile.newInstance("from remote:", identity, null, instLogger);
      add(_identity);
      return true;
    } catch (JSchException e) {
      return false;
    }
  }

  synchronized void remove(Identity identity) {
    if (identities.contains(identity)) {
      identities.removeElement(identity);
      identity.clear();
    } else {
      remove(identity.getPublicKeyBlob());
    }
  }

  @Override
  public synchronized boolean remove(byte[] blob) {
    if (blob == null)
      return false;
    for (int i = 0; i < identities.size(); i++) {
      Identity _identity = identities.elementAt(i);
      byte[] _blob = _identity.getPublicKeyBlob();
      if (_blob == null || !Util.array_equals(blob, _blob))
        continue;
      identities.removeElement(_identity);
      _identity.clear();
      return true;
    }
    return false;
  }

  @Override
  public synchronized void removeAll() {
    for (int i = 0; i < identities.size(); i++) {
      Identity identity = identities.elementAt(i);
      identity.clear();
    }
    identities.removeAllElements();
  }

  private void removeDupulicates() {
    Vector<byte[]> v = new Vector<>();
    int len = identities.size();
    if (len == 0)
      return;
    for (int i = 0; i < len; i++) {
      Identity foo = identities.elementAt(i);
      byte[] foo_blob = foo.getPublicKeyBlob();
      if (foo_blob == null)
        continue;
      for (int j = i + 1; j < len; j++) {
        Identity bar = identities.elementAt(j);
        byte[] bar_blob = bar.getPublicKeyBlob();
        if (bar_blob == null)
          continue;
        if (Util.array_equals(foo_blob, bar_blob) && foo.isEncrypted() == bar.isEncrypted()) {
          v.addElement(foo_blob);
          break;
        }
      }
    }
    for (int i = 0; i < v.size(); i++) {
      remove(v.elementAt(i));
    }
  }
}
