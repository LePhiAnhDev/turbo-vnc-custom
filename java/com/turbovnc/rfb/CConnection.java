package com.turbovnc.rfb;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

import com.turbovnc.network.*;
import com.turbovnc.rdr.*;

public abstract class CConnection extends CMsgHandler {

  public CConnection() {
    csecurity = null;  is = null;  os = null;  reader = null;
    writer = null;  shared = false;
    state = RFBSTATE_UNINITIALISED;
    security = new SecurityClient();
  }

  public void deleteReaderAndWriter() {
    reader = null;
    writer = null;
  }

  public final void initialiseProtocol() {
    state = RFBSTATE_PROTOCOL_VERSION;
  }

  public synchronized void processMsg(boolean benchmark) {
    switch (state) {
      case RFBSTATE_PROTOCOL_VERSION:  processVersionMsg();         break;
      case RFBSTATE_SECURITY_TYPES:    processSecurityTypesMsg();   break;
      case RFBSTATE_SECURITY:          processSecurityMsg();        break;
      case RFBSTATE_SECURITY_RESULT:   processSecurityResultMsg();  break;
      case RFBSTATE_INITIALISATION:    processInitMsg(benchmark);   break;
      case RFBSTATE_NORMAL:            reader.readMsg(params);      break;
      case RFBSTATE_UNINITIALISED:
        throw new ErrorException("CConnection.processMsg: not initialised yet?");
      default:
        throw new ErrorException("CConnection.processMsg: invalid state");
    }
  }

  private void processVersionMsg() {
    if (!alreadyPrintedVersion) {
      vlog.debug("reading protocol version");
      alreadyPrintedVersion = true;
    }

    if (!cp.readVersion(is)) {
      state = RFBSTATE_INVALID;
      throw new ErrorException("Reading version failed: not an RFB server?");
    }
    if (!cp.done) return;

    vlog.info("Server supports RFB protocol version " +
              cp.majorVersion + "." + cp.minorVersion);

    if (cp.majorVersion == 0 && cp.minorVersion == 0) {
      String server = params.server.get();

      if (server == null)
        throw new ErrorException("UltraVNC Repeater detected but VNC server name has not been specified");
      int port = params.port.get();
      if (Hostname.getColonPos(server) < 0 && port > 0)
        server = server + ((port >= 5900 && port <= 5999) ?
                           (":" + (port - 5900)) : ("::" + port));
      vlog.info("Connecting to " + server + " via UltraVNC repeater");
      os.writeBytes(server.getBytes(), 0, Math.min(server.length(), 250));
      if (server.length() < 250) {
        byte[] pad = new byte[250 - server.length()];
        os.writeBytes(pad, 0, pad.length);
      }
      os.flush();
      return;
    }

    if (cp.beforeVersion(3, 3)) {
      String msg = ("Server gave unsupported RFB protocol version " +
                    cp.majorVersion + "." + cp.minorVersion);
      vlog.error(msg);
      state = RFBSTATE_INVALID;
      throw new ErrorException(msg);
    } else if (cp.beforeVersion(3, 7)) {
      cp.setVersion(3, 3);
    } else if (cp.afterVersion(3, 8)) {
      cp.setVersion(3, 8);
    }

    cp.writeVersion(os);
    state = RFBSTATE_SECURITY_TYPES;

    vlog.info("Using RFB protocol version " +
              cp.majorVersion + "." + cp.minorVersion);
  }

  private void processSecurityTypesMsg() {
    vlog.debug("processing security types message");

    int secType = RFB.SECTYPE_INVALID;

    List<Integer> secTypes = new ArrayList<Integer>();
    secTypes = params.secTypes.getEnabled();

    if (cp.isVersion(3, 3)) {

      secType = is.readU32();
      if (secType == RFB.SECTYPE_INVALID) {
        throwConnFailedException();

      } else if (secType == RFB.SECTYPE_NONE ||
                 secType == RFB.SECTYPE_VNCAUTH) {
        Iterator<Integer> i;
        for (i = secTypes.iterator(); i.hasNext();) {
          int refType = i.next();
          if (refType == secType) {
            secType = refType;
            break;
          }
        }

        if (!secTypes.contains(secType))
          secType = RFB.SECTYPE_INVALID;
      } else {
        vlog.error("Unknown RFB 3.3 security type " + secType);
        throw new ErrorException("Unknown RFB 3.3 security type " + secType);
      }

    } else {

      int nServerSecTypes = is.readU8();
      if (nServerSecTypes == 0)
        throwConnFailedException();

      for (int i = 0; i < nServerSecTypes; i++) {
        int serverSecType = is.readU8();
        vlog.debug("Server offers security type " +
                   RFB.secTypeName(serverSecType) + "(" + serverSecType + ")");

        if (serverSecType == RFB.SECTYPE_TIGHT)
          secType = RFB.SECTYPE_TIGHT;

        if (params.sessMgrActive && params.sessMgrAuto.get())
          secType = RFB.SECTYPE_VNCAUTH;

        if (secType == RFB.SECTYPE_INVALID && secType != RFB.SECTYPE_TIGHT) {
          for (Iterator<Integer> j = secTypes.iterator(); j.hasNext();) {
            int refType = j.next();
            if (refType == serverSecType) {
              secType = refType;
              break;
            }
          }
        }
      }

      if (secType != RFB.SECTYPE_INVALID) {
        os.writeU8(secType);
        os.flush();
        vlog.debug("Choosing security type " + RFB.secTypeName(secType) +
                   "(" + secType + ")");
      }
    }

    if (secType == RFB.SECTYPE_INVALID) {
      state = RFBSTATE_INVALID;
      vlog.error("No matching security types");
      throw new ErrorException("No matching security types");
    }

    state = RFBSTATE_SECURITY;
    csecurity = security.getCSecurity(params, secType);
    processSecurityMsg();
  }

  private void processSecurityMsg() {
    if (!alreadyPrintedSecurity) {
      vlog.debug("processing security message");
      alreadyPrintedSecurity = true;
    }

    if (csecurity.processMsg(this)) {
      state = RFBSTATE_SECURITY_RESULT;
      processSecurityResultMsg();
    }
  }

  private void processSecurityResultMsg() {
    if (!alreadyPrintedSecurityResult) {
      vlog.debug("processing security result message");
      alreadyPrintedSecurityResult = true;
    }
    int result;
    if (cp.beforeVersion(3, 8) &&
        (csecurity.getType() == RFB.SECTYPE_NONE ||
         (csecurity instanceof CSecurityRFBTLS &&
          csecurity.getType() == RFB.SECTYPE_TLS_NONE))) {
      result = RFB.AUTH_OK;
    } else {
      if (!is.checkNoWait(1)) return;
      result = is.readU32();
    }
    switch (result) {
      case RFB.AUTH_OK:
        securityCompleted();
        return;
      case RFB.AUTH_FAILED:
        vlog.debug("auth failed");
        break;
      case RFB.AUTH_TOO_MANY:
        vlog.debug("auth failed - too many tries");
        break;
      default:
        throw new ErrorException("Unknown security result from server");
    }
    String reason;
    if (cp.beforeVersion(3, 8))
      reason = "Authentication failure";
    else
      reason = is.readString();
    state = RFBSTATE_INVALID;
    throw new AuthFailureException(reason);
  }

  private void processInitMsg(boolean benchmark) {
    vlog.debug("reading server initialisation");
    reader.readServerInit(benchmark);
  }

  private void throwConnFailedException() {
    state = RFBSTATE_INVALID;
    String reason;
    reason = is.readString();
    throw new ConnFailedException(reason);
  }

  private void securityCompleted() {
    state = RFBSTATE_INITIALISATION;
    reader = new CMsgReader(this, is);
    writer = new CMsgWriter(cp, os);
    vlog.debug("Authentication success!");
    writer.writeClientInit(shared);
  }

  public final void setServerName(String name) {
    serverName = name;
  }

  public final void setStreams(InStream is_, OutStream os_) {
    is = is_;
    os = os_;
  }

  public final void setShared(boolean s) { shared = s; }

  public void setServerPort(int port) {
    serverPort = port;
  }

  public void initSecTypes() {
    nSecTypes = 0;
  }

  public void serverInit() {
    state = RFBSTATE_NORMAL;
    vlog.debug("initialisation done");
  }

  public CSecurity getCurrentCSecurity() { return csecurity; }

  public void setClientSecTypeOrder(boolean csto) {
    clientSecTypeOrder = csto;
  }

  public abstract void handleClipboardAnnounce(boolean available);

  public abstract void handleClipboardData(String data);

  public abstract void handleClipboardRequest();

  public CMsgReader reader() { return reader; }
  public CMsgWriter writer() { return writer; }

  public InStream getInStream() { return is; }
  public OutStream getOutStream() { return os; }

  public String getServerName() { return serverName; }
  public int getServerPort() { return serverPort; }

  public static final int RFBSTATE_UNINITIALISED = 0;
  public static final int RFBSTATE_PROTOCOL_VERSION = 1;
  public static final int RFBSTATE_SECURITY_TYPES = 2;
  public static final int RFBSTATE_SECURITY = 3;
  public static final int RFBSTATE_SECURITY_RESULT = 4;
  public static final int RFBSTATE_INITIALISATION = 5;
  public static final int RFBSTATE_NORMAL = 6;
  public static final int RFBSTATE_INVALID = 7;

  public int state() { return state; }

  protected final void setState(int s) { state = s; }

  public void fence(int flags, int len, byte[] data) {
    super.fence(flags, len, data);

    if ((flags & RFB.FENCE_FLAG_REQUEST) != 0)
      return;

    flags = 0;

    synchronized (this) {
      writer().writeFence(flags, len, data);
    }
  }

  private void throwAuthFailureException() {
    String reason;
    vlog.debug("state=" + state() + ", ver=" + cp.majorVersion + "." +
               cp.minorVersion);
    if (state() == RFBSTATE_SECURITY_RESULT && !cp.beforeVersion(3, 8)) {
      reason = is.readString();
    } else {
      reason = "Authentication failure";
    }
    state = RFBSTATE_INVALID;
    vlog.error(reason);
    throw new AuthFailureException(reason);
  }

  public Socket getSocket() {
    return sock;
  }

  public boolean getUserPasswd(StringBuffer user, StringBuffer password) {
    throw new ErrorException("getUserPasswd() called in base class (this shouldn't happen.)");
  }

  public void announceClipboard(boolean available)
  {
    if (!params.sendClipboard.get())
      return;

    hasLocalClipboard = available;
    unsolicitedClipboardAttempt = false;

    if (available && cp.clipboardSize(RFB.EXTCLIP_FORMAT_UTF8) > 0 &&
        (cp.clipboardFlags() & RFB.EXTCLIP_ACTION_PROVIDE) != 0) {
      vlog.debug("Attempting unsolicited clipboard transfer...");
      unsolicitedClipboardAttempt = true;
      handleClipboardRequest();
      return;
    }

    if ((cp.clipboardFlags() & RFB.EXTCLIP_ACTION_NOTIFY) != 0) {
      writer().writeClipboardNotify(available ? RFB.EXTCLIP_FORMAT_UTF8 : 0);
      return;
    }

    if (available)
      handleClipboardRequest();
  }

  void handleClipboardCaps(int flags, int[] lengths)
  {
    int[] sizes = new int[]{ 0 };

    super.handleClipboardCaps(flags, lengths);

    writer().writeClipboardCaps(RFB.EXTCLIP_FORMAT_UTF8 |
                                RFB.EXTCLIP_ACTION_REQUEST |
                                RFB.EXTCLIP_ACTION_PEEK |
                                RFB.EXTCLIP_ACTION_NOTIFY |
                                RFB.EXTCLIP_ACTION_PROVIDE, sizes);
  }

  void handleClipboardNotify(int flags)
  {
    if (!params.recvClipboard.get())
      return;

    serverClipboard = null;

    if ((flags & RFB.EXTCLIP_FORMAT_UTF8) != 0) {
      hasLocalClipboard = false;
      handleClipboardAnnounce(true);
    } else {
      handleClipboardAnnounce(false);
    }
  }

  void handleClipboardPeek(int flags)
  {
    if (!params.sendClipboard.get())
      return;

    if ((cp.clipboardFlags() & RFB.EXTCLIP_ACTION_NOTIFY) != 0)
      writer().writeClipboardNotify(hasLocalClipboard ?
                                    RFB.EXTCLIP_FORMAT_UTF8 : 0);
  }

  void handleClipboardProvide(int flags, int[] lengths, byte[][] buffers)
  {
    if (!params.recvClipboard.get())
      return;

    if ((flags & RFB.EXTCLIP_FORMAT_UTF8) == 0) {
      vlog.debug("Ignoring Extended Clipboard provide message with " +
                 "unsupported formats 0x" + Integer.toHexString(flags));
      return;
    }

    if (buffers[0][lengths[0] - 1] == 0)
      lengths[0]--;
    serverClipboard =
      Utils.convertLF(new String(buffers[0], 0, lengths[0],
                                 StandardCharsets.UTF_8));

    handleClipboardData(serverClipboard);
  }

  void handleClipboardRequest(int flags)
  {
    if (!params.sendClipboard.get())
      return;

    if ((flags & RFB.EXTCLIP_FORMAT_UTF8) == 0) {
      vlog.debug("Ignoring Extended Clipboard request with unsupported " +
                 "formats 0x" + Integer.toHexString(flags));
      return;
    }
    if (!hasLocalClipboard) {
      vlog.debug("Ignoring unexpected clipboard request");
      return;
    }
    handleClipboardRequest();
  }

  public void requestClipboard()
  {
    if (!params.recvClipboard.get())
      return;

    if (serverClipboard != null) {
      handleClipboardData(serverClipboard);
      return;
    }

    if ((cp.clipboardFlags() & RFB.EXTCLIP_ACTION_REQUEST) != 0)
      writer().writeClipboardRequest(RFB.EXTCLIP_FORMAT_UTF8);
  }

  public void sendClipboardData(String data)
  {
    if (!params.sendClipboard.get())
      return;

    if ((cp.clipboardFlags() & RFB.EXTCLIP_ACTION_PROVIDE) != 0) {
      String filtered = Utils.convertCRLF(data);
      int[] lengths = new int[1];
      byte[][] datas = new byte[1][];

      byte[] filteredBytes = filtered.getBytes(StandardCharsets.UTF_8);
      lengths[0] = filteredBytes.length + 1;
      datas[0] = new byte[filteredBytes.length + 1];
      System.arraycopy(filteredBytes, 0, datas[0], 0, filteredBytes.length);

      if (unsolicitedClipboardAttempt) {
        unsolicitedClipboardAttempt = false;
        if (lengths[0] > cp.clipboardSize(RFB.EXTCLIP_FORMAT_UTF8)) {
          vlog.debug(lengths[0] +
                     "-byte clipboard was too large for unsolicited transfer");
          if ((cp.clipboardFlags() & RFB.EXTCLIP_ACTION_NOTIFY) != 0)
            writer().writeClipboardNotify(RFB.EXTCLIP_FORMAT_UTF8);
          return;
        }
      }

      writer().writeClipboardProvide(RFB.EXTCLIP_FORMAT_UTF8, lengths, datas);
    } else {
      writer().writeClientCutText(data);
    }
  }

  public void serverCutText(String str)
  {
    if (!params.recvClipboard.get())
      return;

    hasLocalClipboard = false;

    serverClipboard = str;

    handleClipboardAnnounce(true);
  }

  InStream is;
  OutStream os;
  protected CMsgReader reader;
  CMsgWriter writer;
  boolean shared;
  protected CSecurity csecurity;
  SecurityClient security;
  int nSecTypes;
  protected int state = RFBSTATE_UNINITIALISED;
  String serverName;
  int serverPort;
  boolean clientSecTypeOrder;
  protected Socket sock;
  boolean alreadyPrintedVersion, alreadyPrintedSecurity;
  boolean alreadyPrintedSecurityResult;

  private boolean hasLocalClipboard, unsolicitedClipboardAttempt;
  private String serverClipboard;

  public Params params;

  static LogWriter vlog = new LogWriter("CConnection");
}
