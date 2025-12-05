package com.jcraft.jsch;

abstract class DHXECKEM extends KeyExchange {

  private static final int SSH_MSG_KEX_ECDH_INIT = 30;
  private static final int SSH_MSG_KEX_ECDH_REPLY = 31;
  private int state;

  byte[] Q_C;

  byte[] V_S;
  byte[] V_C;
  byte[] I_S;
  byte[] I_C;

  byte[] e;

  private Buffer buf;
  private Packet packet;

  private KEM kem;
  private XDH xdh;

  protected String kem_name;
  protected String sha_name;
  protected String curve_name;
  protected int kem_pubkey_len;
  protected int kem_encap_len;
  protected int xec_key_len;

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
    // command + string len + Q_C len
    buf.checkFreeSize(1 + 4 + kem_pubkey_len + xec_key_len);
    buf.putByte((byte) SSH_MSG_KEX_ECDH_INIT);

    try {
      Class<? extends KEM> k = Class.forName(session.getConfig(kem_name)).asSubclass(KEM.class);
      kem = k.getDeclaredConstructor().newInstance();
      kem.init();

      Class<? extends XDH> c = Class.forName(session.getConfig("xdh")).asSubclass(XDH.class);
      xdh = c.getDeclaredConstructor().newInstance();
      xdh.init(curve_name, xec_key_len);

      byte[] kem_public_key_C = kem.getPublicKey();
      byte[] xec_public_key_C = xdh.getQ();
      Q_C = new byte[kem_pubkey_len + xec_key_len];
      System.arraycopy(kem_public_key_C, 0, Q_C, 0, kem_pubkey_len);
      System.arraycopy(xec_public_key_C, 0, Q_C, kem_pubkey_len, xec_key_len);
      buf.putString(Q_C);
    } catch (Exception | NoClassDefFoundError e) {
      throw new JSchException(e.toString(), e);
    }

    if (V_S == null) { // This is a really ugly hack for Session.checkKexes ;-(
      return;
    }

    session.write(packet);

    if (session.getLogger().isEnabled(Logger.INFO)) {
      session.getLogger().log(Logger.INFO, "SSH_MSG_KEX_ECDH_INIT sent");
      session.getLogger().log(Logger.INFO, "expecting SSH_MSG_KEX_ECDH_REPLY");
    }

    state = SSH_MSG_KEX_ECDH_REPLY;
  }

  @Override
  public boolean next(Buffer _buf) throws Exception {
    int i, j;
    switch (state) {
      case SSH_MSG_KEX_ECDH_REPLY:

        j = _buf.getInt();
        j = _buf.getByte();
        j = _buf.getByte();
        if (j != SSH_MSG_KEX_ECDH_REPLY) {
          if (session.getLogger().isEnabled(Logger.ERROR)) {
            session.getLogger().log(Logger.ERROR, "type: must be SSH_MSG_KEX_ECDH_REPLY " + j);
          }
          return false;
        }

        K_S = _buf.getString();

        byte[] Q_S = _buf.getString();
        if (Q_S.length != kem_encap_len + xec_key_len) {
          return false;
        }

        byte[] encapsulation = new byte[kem_encap_len];
        byte[] xec_public_key_S = new byte[xec_key_len];
        System.arraycopy(Q_S, 0, encapsulation, 0, kem_encap_len);
        System.arraycopy(Q_S, kem_encap_len, xec_public_key_S, 0, xec_key_len);

        if (!xdh.validate(xec_public_key_S)) {
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
          tmp = normalize(xdh.getSecret(xec_public_key_S));
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
        buf.putString(Q_C);
        buf.putString(Q_S);
        byte[] foo = new byte[buf.getLength()];
        buf.getByte(foo);

        sha.update(foo, 0, foo.length);
        sha.update(K, 0, K.length);
        H = sha.digest();

        i = 0;
        j = 0;
        j = ((K_S[i++] << 24) & 0xff000000) | ((K_S[i++] << 16) & 0x00ff0000)
            | ((K_S[i++] << 8) & 0x0000ff00) | ((K_S[i++]) & 0x000000ff);
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
