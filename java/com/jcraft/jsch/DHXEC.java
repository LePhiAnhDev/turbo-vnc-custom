package com.jcraft.jsch;

abstract class DHXEC extends KeyExchange {

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

  private XDH xdh;

  protected String sha_name;
  protected String curve_name;
  protected int key_len;

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
    buf.putByte((byte) SSH_MSG_KEX_ECDH_INIT);

    try {
      Class<? extends XDH> c = Class.forName(session.getConfig("xdh")).asSubclass(XDH.class);
      xdh = c.getDeclaredConstructor().newInstance();
      xdh.init(curve_name, key_len);

      Q_C = xdh.getQ();
      buf.putString(Q_C);
    } catch (Exception | NoClassDefFoundError e) {
      throw new JSchException(e.toString(), e);
    }

    if (V_S == null) {
      return;
    }

    session.write(packet);

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
          return false;
        }

        K_S = _buf.getString();

        byte[] Q_S = _buf.getString();

        if (!xdh.validate(Q_S)) {
          return false;
        }

        K = encodeAsMPInt(normalize(xdh.getSecret(Q_S)));

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
