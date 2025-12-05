package com.jcraft.jsch.jce;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.SignatureRSA;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

abstract class SignatureRSAN implements SignatureRSA {

  Signature signature;
  KeyFactory keyFactory;

  abstract String getName();

  @Override
  public void init() throws Exception {
    String name = getName();
    String foo = "SHA1withRSA";
    if (name.equals("rsa-sha2-256") || name.equals("ssh-rsa-sha256@ssh.com"))
      foo = "SHA256withRSA";
    else if (name.equals("rsa-sha2-512") || name.equals("ssh-rsa-sha512@ssh.com"))
      foo = "SHA512withRSA";
    else if (name.equals("ssh-rsa-sha384@ssh.com"))
      foo = "SHA384withRSA";
    else if (name.equals("ssh-rsa-sha224@ssh.com"))
      foo = "SHA224withRSA";
    signature = Signature.getInstance(foo);
    keyFactory = KeyFactory.getInstance("RSA");
  }

  @Override
  public void setPubKey(byte[] e, byte[] n) throws Exception {
    RSAPublicKeySpec rsaPubKeySpec = new RSAPublicKeySpec(new BigInteger(n), new BigInteger(e));
    PublicKey pubKey = keyFactory.generatePublic(rsaPubKeySpec);
    signature.initVerify(pubKey);
  }

  @Override
  public void setPrvKey(byte[] d, byte[] n) throws Exception {
    RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(new BigInteger(n), new BigInteger(d));
    PrivateKey prvKey = keyFactory.generatePrivate(rsaPrivKeySpec);
    signature.initSign(prvKey);
  }

  @Override
  public byte[] sign() throws Exception {
    byte[] sig = signature.sign();
    return sig;
  }

  @Override
  public void update(byte[] foo) throws Exception {
    signature.update(foo);
  }

  @Override
  public boolean verify(byte[] sig) throws Exception {
    int i = 0;
    int j = 0;
    byte[] tmp;
    Buffer buf = new Buffer(sig);

    String foo = new String(buf.getString(), StandardCharsets.UTF_8);
    if (foo.equals("ssh-rsa") || foo.equals("rsa-sha2-256") || foo.equals("rsa-sha2-512")
        || foo.equals("ssh-rsa-sha224@ssh.com") || foo.equals("ssh-rsa-sha256@ssh.com")
        || foo.equals("ssh-rsa-sha384@ssh.com") || foo.equals("ssh-rsa-sha512@ssh.com")) {
      if (!foo.equals(getName()))
        return false;
      j = buf.getInt();
      i = buf.getOffSet();
      tmp = new byte[j];
      System.arraycopy(sig, i, tmp, 0, j);
      sig = tmp;
    }

    return signature.verify(sig);
  }
}
