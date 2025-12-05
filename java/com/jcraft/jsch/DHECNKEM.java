package com.jcraft.jsch;

abstract class DHECNKEM extends KeyExchange {

  private static final int SSH_MSG_KEX_HYBRID_INIT = 30;
  private static final int SSH_MSG_KEX_HYBRID_REPLY = 31;
  private int state;

  byte[] C_INIT;

  byte[] V_S;
  byte[] V_C;
  byte[] I_S;
  byte[] I_C;

  byte[] e;

  private Buffer buf;
  private Packet packet;

  private KEM kem;
  private ECDH ecdh;

  protected String kem_name;
  protected String sha_name;
  protected int kem_pubkey_len;
  protected int kem_encap_len;
  protected int ecdh_key_size;
  protected int ecdh_key_len;

  @Override
  public void init(Session session, byte[] V_S, byte[] V_C, byte[] I_S, byte[] I_C)
      throws Exception {
    this.V_S = V_S;
    this.V_C = V_C;
    this.I_S = I_S;
    this.I_C = I_C;

    try {
      Class<? extends HASH> c = Class.forName(session.getConfig(sha_name)).asSubclass(HASH.class);
      sha = c.getDeclaredConstructor().newInstance();
      sha.init();
    } catch (Exception e) {
      throw new JSchException(e.toString(), e);
    }

    buf = new Buffer();
    packet = new Packet(buf);

    packet.reset();
    buf.checkFreeSize(1 + 4 + kem_pubkey_len + ecdh_key_len);
    buf.putByte((byte) SSH_MSG_KEX_HYBRID_INIT);

    try {
      Class<? extends KEM> k = Class.forName(session.getConfig(kem_name)).asSubclass(KEM.class);
      kem = k.getDeclaredConstructor().newInstance();
      kem.init();

      Class<? extends ECDH> c =
          Class.forName(session.getConfig("ecdh-sha2-nistp")).asSubclass(ECDH.class);
      ecdh = c.getDeclaredConstructor().newInstance();
      ecdh.init(ecdh_key_size);

      byte[] kem_public_key_C = kem.getPublicKey();
      byte[] ecdh_public_key_C = ecdh.getQ();
      C_INIT = new byte[kem_pubkey_len + ecdh_key_len];
      System.arraycopy(kem_public_key_C, 0, C_INIT, 0, kem_pubkey_len);
      System.arraycopy(ecdh_public_key_C, 0, C_INIT, kem_pubkey_len, ecdh_key_len);
      buf.putString(C_INIT);
    } catch (Exception e) {
      throw new JSchException(e.toString(), e);
    }

    if (V_S == null) {
      return;
    }

    session.write(packet);

    state = SSH_MSG_KEX_HYBRID_REPLY;
  }

  @Override
  public boolean next(Buffer _buf) throws Exception {
    int i, j;
    switch (state) {
      case SSH_MSG_KEX_HYBRID_REPLY:
        j = _buf.getInt();
        j = _buf.getByte();
        j = _buf.getByte();
        if (j != SSH_MSG_KEX_HYBRID_REPLY) {
          return false;
        }

        K_S = _buf.getString();

        byte[] S_REPLY = _buf.getString();
        if (S_REPLY.length != kem_encap_len + ecdh_key_len) {
          return false;
        }

        byte[] encapsulation = new byte[kem_encap_len];
        byte[] ecdh_public_key_S = new byte[ecdh_key_len];
        System.arraycopy(S_REPLY, 0, encapsulation, 0, kem_encap_len);
        System.arraycopy(S_REPLY, kem_encap_len, ecdh_public_key_S, 0, ecdh_key_len);

        byte[][] r_s = KeyPairECDSA.fromPoint(ecdh_public_key_S);

        if (!ecdh.validate(r_s[0], r_s[1])) {
          return false;
        }

        byte[] tmp = null;
        try {
          tmp = kem.decapsulate(encapsulation);
          sha.update(tmp, 0, tmp.length);
        } finally {
          Util.bzero(tmp);
        }
        try {
          tmp = normalize(ecdh.getSecret(r_s[0], r_s[1]));
          sha.update(tmp, 0, tmp.length);
        } finally {
          Util.bzero(tmp);
        }
        K = encodeAsString(sha.digest());

        byte[] sig_of_H = _buf.getString();

        buf.reset();
        buf.putString(V_C);
        buf.putString(V_S);
        buf.putString(I_C);
        buf.putString(I_S);
        buf.putString(K_S);
        buf.putString(C_INIT);
        buf.putString(S_REPLY);
        byte[] foo = new byte[buf.getLength()];
        buf.getByte(foo);

        sha.update(foo, 0, foo.length);
        sha.update(K, 0, K.length);
        H = sha.digest();

        i = 0;
        j = 0;
        j = ((K_S[i++] << 24) & 0xff000000)
            | ((K_S[i++] << 16) & 0x00ff0000)
            | ((K_S[i++] << 8) & 0x0000ff00)
            | ((K_S[i++]) & 0x000000ff);

        String alg = Util.byte2str(K_S, i, j);
        i += j;

        boolean result = verify(alg, K_S, i, sig_of_H);

        state = STATE_END;
        return result;
    }
    return false;
  }

  @Override
  public int getState() {
    return state;
  }
}
