package com.turbovnc.rfb;

import com.turbovnc.rdr.ErrorException;

public class SecurityClient {

  public CSecurity getCSecurity(Params params, int secType) {
    assert (msg != null);

    if (!params.secTypes.isSupported(secType))
      throw new ErrorException("Security type not supported");

    switch (secType) {
      case RFB.SECTYPE_NONE:      return (new CSecurityNone());
      case RFB.SECTYPE_VNCAUTH:   return (new CSecurityVncAuth());
      case RFB.SECTYPE_TIGHT:     return (new CSecurityTight(this));
      case RFB.SECTYPE_TLS:       return (new CSecurityRFBTLS(this));
      case RFB.SECTYPE_VENCRYPT:  return (new CSecurityVeNCrypt(this));
      case RFB.SECTYPE_PLAIN:     return (new CSecurityPlain());
      case RFB.SECTYPE_TLS_NONE:
        return (new CSecurityStack(RFB.SECTYPE_TLS_NONE, "TLSNone",
                new CSecurityTLS(true), null));
      case RFB.SECTYPE_TLS_VNC:
        return (new CSecurityStack(RFB.SECTYPE_TLS_VNC, "TLSVnc",
                new CSecurityTLS(true), new CSecurityVncAuth()));
      case RFB.SECTYPE_TLS_PLAIN:
        return (new CSecurityStack(RFB.SECTYPE_TLS_PLAIN, "TLSPlain",
                new CSecurityTLS(true), new CSecurityPlain()));
      case RFB.SECTYPE_X509_NONE:
        return (new CSecurityStack(RFB.SECTYPE_X509_NONE, "X509None",
                new CSecurityTLS(false), null));
      case RFB.SECTYPE_X509_VNC:
        return (new CSecurityStack(RFB.SECTYPE_X509_VNC, "X509Vnc",
                new CSecurityTLS(false), new CSecurityVncAuth()));
      case RFB.SECTYPE_X509_PLAIN:
        return (new CSecurityStack(RFB.SECTYPE_X509_PLAIN, "X509Plain",
                new CSecurityTLS(false), new CSecurityPlain()));
      default:
        throw new ErrorException("Security type not supported");
    }

  }

  String msg = null;
}
