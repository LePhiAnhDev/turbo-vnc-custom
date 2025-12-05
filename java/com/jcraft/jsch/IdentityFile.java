package com.jcraft.jsch;

class IdentityFile implements Identity {
  private KeyPair kpair;
  private String identity;

  static IdentityFile newInstance(String prvfile, String pubfile, JSch.InstanceLogger instLogger)
      throws JSchException {
    KeyPair kpair = KeyPair.load(instLogger, prvfile, pubfile);
    return new IdentityFile(prvfile, kpair);
  }

  static IdentityFile newInstance(String name, byte[] prvkey, byte[] pubkey,
      JSch.InstanceLogger instLogger) throws JSchException {
    KeyPair kpair = KeyPair.load(instLogger, prvkey, pubkey);
    return new IdentityFile(name, kpair);
  }

  private IdentityFile(String name, KeyPair kpair) {
    this.identity = name;
    this.kpair = kpair;
  }

  @Override
  public boolean setPassphrase(byte[] passphrase) throws JSchException {
    return kpair.decrypt(passphrase);
  }

  @Override
  public byte[] getPublicKeyBlob() {
    return kpair.getPublicKeyBlob();
  }

  @Override
  public String getFingerPrint() {
    return kpair.getFingerPrint();
  }

  @Override
  public byte[] getSignature(byte[] data) {
    return kpair.getSignature(data);
  }

  @Override
  public byte[] getSignature(byte[] data, String alg) {
    return kpair.getSignature(data, alg);
  }

  @Override
  public String getAlgName() {
    return kpair.getKeyTypeString();
  }

  @Override
  public String getName() {
    return identity;
  }

  @Override
  public boolean isEncrypted() {
    return kpair.isEncrypted();
  }

  @Override
  public void clear() {
    kpair.dispose();
    kpair = null;
  }

  public KeyPair getKeyPair() {
    return kpair;
  }
}
