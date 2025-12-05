package com.jcraft.jsch;

class AgentIdentity implements Identity {

  private AgentProxy agent;
  private byte[] blob;
  private String comment;
  private String algname;
  private HASH hash;

  AgentIdentity(AgentProxy agent, byte[] blob, String comment) {
    this.agent = agent;
    this.blob = blob;
    this.comment = comment;
    algname = Util.byte2str((new Buffer(blob)).getString());
  }

  private HASH genHash() {
    try {
      Class c = Class.forName(JSch.getConfig("md5"));
      hash = (HASH)(c.getDeclaredConstructor().newInstance());
      hash.init();
    } catch (Exception e) {
    }
    return hash;
  }

  @Override
  public boolean setPassphrase(byte[] passphrase) throws JSchException {
    return true;
  }

  @Override
  public byte[] getPublicKeyBlob() {
    return blob;
  }

  @Override
  public String getFingerPrint() {
    if (hash == null) hash = genHash();
    if (blob == null) return null;
    return Util.getFingerPrint(hash, blob, false, true);
  }

  @Override
  public byte[] getSignature(byte[] data) {
    return agent.sign(blob, data, null);
  }

  @Override
  public byte[] getSignature(byte[] data, String alg) {
    return agent.sign(blob, data, alg);
  }

  @Override
  public String getAlgName() {
    return algname;
  }

  @Override
  public String getName() {
    return comment;
  }

  @Override
  public boolean isEncrypted() {
    return false;
  }

  @Override
  public void clear() {}
}
