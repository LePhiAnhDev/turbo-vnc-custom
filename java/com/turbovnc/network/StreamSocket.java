package com.turbovnc.network;

import java.io.InputStream;
import java.io.OutputStream;

import com.turbovnc.rdr.*;

public class StreamSocket extends Socket {

  public StreamSocket(InputStream in, OutputStream out, boolean own) {
    descriptor = new StreamDescriptor(in, out);
    instream = new FdInStream(descriptor);
    outstream = new FdOutStream(descriptor);
    ownStreams = own;
  }

  public int getMyPort() {
    return -1;
  }

  public String getPeerAddress() {
    return "";
  }

  public String getPeerName() {
    return "";
  }

  public int getPeerPort() {
    return -1;
  }

  public String getPeerEndpoint() {
    return "";
  }

  public boolean sameMachine() {
    return false;
  }

  public void shutdown() {
    super.shutdown();
    descriptor.close();
  }

  public void close() {
    descriptor.close();
  }

  public boolean isConnected() {
    return true;
  }

  public int getSockPort() {
    return -1;
  }

  private StreamDescriptor descriptor;
}
