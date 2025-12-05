package com.jcraft.jsch.jgss;

import com.jcraft.jsch.JSchException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

public class GSSContextKrb5 implements com.jcraft.jsch.GSSContext {

  private static final String pUseSubjectCredsOnly = "javax.security.auth.useSubjectCredsOnly";
  private static String useSubjectCredsOnly = getSystemProperty(pUseSubjectCredsOnly);

  private GSSContext context = null;

  @Override
  public void create(String user, String host) throws JSchException {
    try {
      Oid krb5 = new Oid("1.2.840.113554.1.2.2");
      Oid principalName = GSSName.NT_HOSTBASED_SERVICE;

      GSSManager mgr = GSSManager.getInstance();
      GSSCredential crd = null;

      String cname = host;
      try {
        cname = InetAddress.getByName(cname).getCanonicalHostName();
      } catch (UnknownHostException e) {
      }

      GSSName _host = mgr.createName("host@" + cname, principalName);
      context = mgr.createContext(_host, krb5, crd, GSSContext.DEFAULT_LIFETIME);

      context.requestMutualAuth(true);
      context.requestConf(true);
      context.requestInteg(true);
      context.requestCredDeleg(true);
      context.requestAnonymity(false);

    } catch (GSSException ex) {
      throw new JSchException(ex.toString(), ex);
    }
  }

  @Override
  public boolean isEstablished() {
    return context.isEstablished();
  }

  @Override
  public byte[] init(byte[] token, int s, int l) throws JSchException {
    try {
      if (useSubjectCredsOnly == null) {
        setSystemProperty(pUseSubjectCredsOnly, "false");
      }
      return context.initSecContext(token, 0, l);
    } catch (GSSException ex) {
      throw new JSchException(ex.toString(), ex);
    } catch (SecurityException ex) {
      throw new JSchException(ex.toString(), ex);
    } finally {
      if (useSubjectCredsOnly == null) {
        setSystemProperty(pUseSubjectCredsOnly, "true");
      }
    }
  }

  @Override
  public byte[] getMIC(byte[] message, int s, int l) {
    try {
      MessageProp prop = new MessageProp(0, true);
      return context.getMIC(message, s, l, prop);
    } catch (GSSException ex) {
      return null;
    }
  }

  @Override
  public void dispose() {
    try {
      context.dispose();
    } catch (GSSException ex) {
    }
  }

  private static String getSystemProperty(String key) {
    try {
      return System.getProperty(key);
    } catch (Exception e) {
      return null;
    }
  }

  private static void setSystemProperty(String key, String value) {
    try {
      System.setProperty(key, value);
    } catch (Exception e) {
    }
  }
}
