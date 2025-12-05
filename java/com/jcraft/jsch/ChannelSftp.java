package com.jcraft.jsch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Vector;

public class ChannelSftp extends ChannelSession {

  private static final int LOCAL_MAXIMUM_PACKET_SIZE = 32 * 1024;
  private static final int LOCAL_WINDOW_SIZE_MAX = (64 * LOCAL_MAXIMUM_PACKET_SIZE);

  private static final byte SSH_FXP_INIT = 1;
  private static final byte SSH_FXP_VERSION = 2;
  private static final byte SSH_FXP_OPEN = 3;
  private static final byte SSH_FXP_CLOSE = 4;
  private static final byte SSH_FXP_READ = 5;
  private static final byte SSH_FXP_WRITE = 6;
  private static final byte SSH_FXP_LSTAT = 7;
  private static final byte SSH_FXP_FSTAT = 8;
  private static final byte SSH_FXP_SETSTAT = 9;
  private static final byte SSH_FXP_FSETSTAT = 10;
  private static final byte SSH_FXP_OPENDIR = 11;
  private static final byte SSH_FXP_READDIR = 12;
  private static final byte SSH_FXP_REMOVE = 13;
  private static final byte SSH_FXP_MKDIR = 14;
  private static final byte SSH_FXP_RMDIR = 15;
  private static final byte SSH_FXP_REALPATH = 16;
  private static final byte SSH_FXP_STAT = 17;
  private static final byte SSH_FXP_RENAME = 18;
  private static final byte SSH_FXP_READLINK = 19;
  private static final byte SSH_FXP_SYMLINK = 20;
  private static final byte SSH_FXP_STATUS = 101;
  private static final byte SSH_FXP_HANDLE = 102;
  private static final byte SSH_FXP_DATA = 103;
  private static final byte SSH_FXP_NAME = 104;
  private static final byte SSH_FXP_ATTRS = 105;
  private static final byte SSH_FXP_EXTENDED = (byte) 200;
  private static final byte SSH_FXP_EXTENDED_REPLY = (byte) 201;

  private static final int SSH_FXF_READ = 0x00000001;
  private static final int SSH_FXF_WRITE = 0x00000002;
  private static final int SSH_FXF_APPEND = 0x00000004;
  private static final int SSH_FXF_CREAT = 0x00000008;
  private static final int SSH_FXF_TRUNC = 0x00000010;
  private static final int SSH_FXF_EXCL = 0x00000020;

  private static final int SSH_FILEXFER_ATTR_SIZE = 0x00000001;
  private static final int SSH_FILEXFER_ATTR_UIDGID = 0x00000002;
  private static final int SSH_FILEXFER_ATTR_PERMISSIONS = 0x00000004;
  private static final int SSH_FILEXFER_ATTR_ACMODTIME = 0x00000008;
  private static final int SSH_FILEXFER_ATTR_EXTENDED = 0x80000000;

  public static final int SSH_FX_OK = 0;
  public static final int SSH_FX_EOF = 1;
  public static final int SSH_FX_NO_SUCH_FILE = 2;
  public static final int SSH_FX_PERMISSION_DENIED = 3;
  public static final int SSH_FX_FAILURE = 4;
  public static final int SSH_FX_BAD_MESSAGE = 5;
  public static final int SSH_FX_NO_CONNECTION = 6;
  public static final int SSH_FX_CONNECTION_LOST = 7;
  public static final int SSH_FX_OP_UNSUPPORTED = 8;
  
  private static final int MAX_MSG_LENGTH = 256 * 1024;

  public static final int OVERWRITE = 0;
  public static final int RESUME = 1;
  public static final int APPEND = 2;

  private boolean interactive = false;
  private int seq = 1;
  private int[] ackid = new int[1];

  private Buffer buf;
  private Packet packet;

  private Buffer obuf;
  private Packet opacket;

  private int client_version = 3;
  private int server_version = 3;
  private String version = String.valueOf(client_version);

  private Hashtable<String, String> extensions = null;
  private InputStream io_in = null;

  private boolean extension_posix_rename = false;
  private boolean extension_statvfs = false;
  
  private boolean extension_hardlink = false;

  private static final String file_separator = File.separator;
  private static final char file_separatorc = File.separatorChar;
  private static boolean fs_is_bs = (byte) File.separatorChar == '\\';

  private String cwd;
  private String home;
  private String lcwd;

  private Charset fEncoding = StandardCharsets.UTF_8;
  private boolean fEncoding_is_utf8 = true;

  private boolean useWriteFlushWorkaround = true;

  private RequestQueue rq = new RequestQueue(16);

  public void setBulkRequests(int bulk_requests) throws JSchException {
    if (bulk_requests > 0)
      rq = new RequestQueue(bulk_requests);
    else
      throw new JSchException("setBulkRequests: " + bulk_requests + " must be greater than 0.");
  }

  public int getBulkRequests() {
    return rq.size();
  }

  public void setUseWriteFlushWorkaround(boolean useWriteFlushWorkaround) {
    this.useWriteFlushWorkaround = useWriteFlushWorkaround;
  }

  public boolean getUseWriteFlushWorkaround() {
    return useWriteFlushWorkaround;
  }

  public ChannelSftp() {
    super();
    lwsize_max = LOCAL_WINDOW_SIZE_MAX;
    lwsize = LOCAL_WINDOW_SIZE_MAX;
    lmpsize = LOCAL_MAXIMUM_PACKET_SIZE;
  }

  @Override
  void init() {}

  @Override
  public void start() throws JSchException {
    try {

      PipedOutputStream pos = new PipedOutputStream();
      io.setOutputStream(pos);
      PipedInputStream pis = new MyPipedInputStream(pos, rq.size() * rmpsize);
      io.setInputStream(pis);

      io_in = io.in;

      if (io_in == null) {
        throw new JSchException("channel is down");
      }

      Request request = new RequestSftp();
      request.request(getSession(), this);

      buf = new Buffer(lmpsize);
      packet = new Packet(buf);

      obuf = new Buffer(rmpsize);
      opacket = new Packet(obuf);

      int i = 0;
      int length;
      int type;
      byte[] str;

      sendINIT();

      Header header = new Header();
      header = header(buf, header);
      length = header.length;
      if (length > MAX_MSG_LENGTH) {
        throw new SftpException(SSH_FX_FAILURE, "Received message is too long: " + length);
      }
      type = header.type; 
      server_version = header.rid;
      
      extensions = new Hashtable<>();
      if (length > 0) {
        
        fill(buf, length);
        byte[] extension_name = null;
        byte[] extension_data = null;
        while (length > 0) {
          extension_name = buf.getString();
          length -= (4 + extension_name.length);
          extension_data = buf.getString();
          length -= (4 + extension_data.length);
          extensions.put(Util.byte2str(extension_name), Util.byte2str(extension_data));
        }
      }

      if (extensions.get("posix-rename@openssh.com") != null
          && extensions.get("posix-rename@openssh.com").equals("1")) {
        extension_posix_rename = true;
      }

      if (extensions.get("statvfs@openssh.com") != null
          && extensions.get("statvfs@openssh.com").equals("2")) {
        extension_statvfs = true;
      }

      if (extensions.get("hardlink@openssh.com") != null
          && extensions.get("hardlink@openssh.com").equals("1")) {
        extension_hardlink = true;
      }

      lcwd = new File(".").getCanonicalPath();
    } catch (Exception e) {
      
      if (e instanceof JSchException)
        throw (JSchException) e;
      throw new JSchException(e.toString(), e);
    }
  }

  public void quit() {
    disconnect();
  }

  public void exit() {
    disconnect();
  }

  public void lcd(String path) throws SftpException {
    path = localAbsolutePath(path);
    if ((new File(path)).isDirectory()) {
      try {
        path = (new File(path)).getCanonicalPath();
      } catch (Exception e) {
      }
      lcwd = path;
      return;
    }
    throw new SftpException(SSH_FX_NO_SUCH_FILE, "No such directory");
  }

  public void cd(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);
      path = isUnique(path);

      byte[] str = _realpath(path);
      SftpATTRS attr = _stat(str);

      if ((attr.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0) {
        throw new SftpException(SSH_FX_FAILURE, "Can't change directory: " + path);
      }
      if (!attr.isDir()) {
        throw new SftpException(SSH_FX_FAILURE, "Can't change directory: " + path);
      }

      setCwd(Util.byte2str(str, fEncoding));
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void put(String src, String dst) throws SftpException {
    put(src, dst, null, OVERWRITE);
  }

  public void put(String src, String dst, int mode) throws SftpException {
    put(src, dst, null, mode);
  }

  public void put(String src, String dst, SftpProgressMonitor monitor) throws SftpException {
    put(src, dst, monitor, OVERWRITE);
  }

  public void put(String src, String dst, SftpProgressMonitor monitor, int mode)
      throws SftpException {

    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      src = localAbsolutePath(src);
      dst = remoteAbsolutePath(dst);

      Vector<String> v = glob_remote(dst);
      int vsize = v.size();
      if (vsize != 1) {
        if (vsize == 0) {
          if (isPattern(dst))
            throw new SftpException(SSH_FX_FAILURE, dst);
          else
            dst = Util.unquote(dst);
        }
        throw new SftpException(SSH_FX_FAILURE, v.toString());
      } else {
        dst = v.elementAt(0);
      }

      boolean isRemoteDir = isRemoteDir(dst);

      v = glob_local(src);
      vsize = v.size();

      StringBuilder dstsb = null;
      if (isRemoteDir) {
        if (!dst.endsWith("/")) {
          dst += "/";
        }
        dstsb = new StringBuilder(dst);
      } else if (vsize > 1) {
        throw new SftpException(SSH_FX_FAILURE,
            "Copying multiple files, but the destination is missing or a file.");
      }

      for (int j = 0; j < vsize; j++) {
        String _src = v.elementAt(j);
        String _dst = null;
        if (isRemoteDir) {
          int i = _src.lastIndexOf(file_separatorc);
          if (fs_is_bs) {
            int ii = _src.lastIndexOf('/');
            if (ii != -1 && ii > i)
              i = ii;
          }
          if (i == -1)
            dstsb.append(_src);
          else
            dstsb.append(_src.substring(i + 1));
          _dst = dstsb.toString();
          dstsb.delete(dst.length(), _dst.length());
        } else {
          _dst = dst;
        }
        
        long size_of_dst = 0;
        if (mode == RESUME) {
          try {
            SftpATTRS attr = _stat(_dst);
            size_of_dst = attr.getSize();
          } catch (Exception eee) {
            
          }
          long size_of_src = new File(_src).length();
          if (size_of_src < size_of_dst) {
            throw new SftpException(SSH_FX_FAILURE, "failed to resume for " + _dst);
          }
          if (size_of_src == size_of_dst) {
            return;
          }
        }

        if (monitor != null) {
          monitor.init(SftpProgressMonitor.PUT, _src, _dst, (new File(_src)).length());
          if (mode == RESUME) {
            monitor.count(size_of_dst);
          }
        }
        try (InputStream fis = new FileInputStream(_src)) {
          _put(fis, _dst, monitor, mode);
        }
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void put(InputStream src, String dst) throws SftpException {
    put(src, dst, null, OVERWRITE);
  }

  public void put(InputStream src, String dst, int mode) throws SftpException {
    put(src, dst, null, mode);
  }

  public void put(InputStream src, String dst, SftpProgressMonitor monitor) throws SftpException {
    put(src, dst, monitor, OVERWRITE);
  }

  public void put(InputStream src, String dst, SftpProgressMonitor monitor, int mode)
      throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      dst = remoteAbsolutePath(dst);

      Vector<String> v = glob_remote(dst);
      int vsize = v.size();
      if (vsize != 1) {
        if (vsize == 0) {
          if (isPattern(dst))
            throw new SftpException(SSH_FX_FAILURE, dst);
          else
            dst = Util.unquote(dst);
        }
        throw new SftpException(SSH_FX_FAILURE, v.toString());
      } else {
        dst = v.elementAt(0);
      }

      if (monitor != null) {
        monitor.init(SftpProgressMonitor.PUT, "-", dst, SftpProgressMonitor.UNKNOWN_SIZE);
      }

      _put(src, dst, monitor, mode);
    } catch (Exception e) {
      if (e instanceof SftpException) {
        if (((SftpException) e).id == SSH_FX_FAILURE && isRemoteDir(dst)) {
          throw new SftpException(SSH_FX_FAILURE, dst + " is a directory");
        }
        throw (SftpException) e;
      }
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void _put(InputStream src, String dst, SftpProgressMonitor monitor, int mode)
      throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      byte[] dstb = Util.str2byte(dst, fEncoding);
      long skip = 0;
      if (mode == RESUME || mode == APPEND) {
        try {
          SftpATTRS attr = _stat(dstb);
          skip = attr.getSize();
        } catch (Exception eee) {
          
        }
      }
      if (mode == RESUME && skip > 0) {
        long skipped = src.skip(skip);
        if (skipped < skip) {
          throw new SftpException(SSH_FX_FAILURE, "failed to resume for " + dst);
        }
      }

      if (mode == OVERWRITE) {
        sendOPENW(dstb);
      } else {
        sendOPENA(dstb);
      }

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS && type != SSH_FXP_HANDLE) {
        throw new SftpException(SSH_FX_FAILURE, "invalid type=" + type);
      }
      if (type == SSH_FXP_STATUS) {
        int i = buf.getInt();
        throwStatusError(buf, i);
      }
      byte[] handle = buf.getString(); 
      byte[] data = null;

      boolean dontcopy = true;

      int buffer_margin = session.getBufferMargin();
      if (!dontcopy) { 
        data = new byte[obuf.buffer.length - (5 + 13 + 21 + handle.length + buffer_margin)];
      }

      long offset = 0;
      if (mode == RESUME || mode == APPEND) {
        offset += skip;
      }

      int startid = seq;
      int ackcount = 0;
      int _s = 0;
      int _datalen = 0;

      if (!dontcopy) { 
        _datalen = data.length;
      } else {
        data = obuf.buffer;
        _s = 5 + 13 + 21 + handle.length;
        _datalen = obuf.buffer.length - _s - buffer_margin;
      }

      int bulk_requests = rq.size();

      while (true) {
        int nread = 0;
        int count = 0;
        int s = _s;
        int datalen = _datalen;

        do {
          nread = src.read(data, s, datalen);
          if (nread > 0) {
            s += nread;
            datalen -= nread;
            count += nread;
          }
        } while (datalen > 0 && nread > 0);
        if (count <= 0)
          break;

        int foo = count;
        while (foo > 0) {
          if ((seq - 1) == startid || ((seq - startid) - ackcount) >= bulk_requests) {
            while (((seq - startid) - ackcount) >= bulk_requests) {
              if (checkStatus(ackid, header)) {
                int _ackid = ackid[0];
                if (startid > _ackid || _ackid > seq - 1) {
                  if (_ackid == seq) {
                    if (getSession().getLogger().isEnabled(Logger.ERROR)) {
                      getSession().getLogger().log(Logger.ERROR,
                          "ack error: startid=" + startid + " seq=" + seq + " _ackid=" + _ackid);
                    }
                  } else {
                    throw new SftpException(SSH_FX_FAILURE,
                        "ack error: startid=" + startid + " seq=" + seq + " _ackid=" + _ackid);
                  }
                }
                ackcount++;
              } else {
                break;
              }
            }
          }
          if (dontcopy) {
            foo -= sendWRITE(handle, offset, data, 0, foo);
            if (data != obuf.buffer) {
              data = obuf.buffer;
              _datalen = obuf.buffer.length - _s - buffer_margin;
            }
          } else {
            foo -= sendWRITE(handle, offset, data, _s, foo);
          }
        }
        offset += count;
        if (monitor != null && !monitor.count(count)) {
          break;
        }
      }
      int _ackcount = seq - startid;
      while (_ackcount > ackcount) {
        if (!checkStatus(null, header)) {
          break;
        }
        ackcount++;
      }
      if (monitor != null)
        monitor.end();
      _sendCLOSE(handle, header);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public OutputStream put(String dst) throws SftpException {
    return put(dst, (SftpProgressMonitor) null, OVERWRITE);
  }

  public OutputStream put(String dst, final int mode) throws SftpException {
    return put(dst, (SftpProgressMonitor) null, mode);
  }

  public OutputStream put(String dst, final SftpProgressMonitor monitor, final int mode)
      throws SftpException {
    return put(dst, monitor, mode, 0);
  }

  public OutputStream put(String dst, final SftpProgressMonitor monitor, final int mode,
      long offset) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      dst = remoteAbsolutePath(dst);
      dst = isUnique(dst);

      if (isRemoteDir(dst)) {
        throw new SftpException(SSH_FX_FAILURE, dst + " is a directory");
      }

      byte[] dstb = Util.str2byte(dst, fEncoding);

      long skip = 0;
      if (mode == RESUME || mode == APPEND) {
        try {
          SftpATTRS attr = _stat(dstb);
          skip = attr.getSize();
        } catch (Exception eee) {
          
        }
      }

      if (monitor != null) {
        monitor.init(SftpProgressMonitor.PUT, "-", dst, SftpProgressMonitor.UNKNOWN_SIZE);
      }

      if (mode == OVERWRITE) {
        sendOPENW(dstb);
      } else {
        sendOPENA(dstb);
      }

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS && type != SSH_FXP_HANDLE) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      if (type == SSH_FXP_STATUS) {
        int i = buf.getInt();
        throwStatusError(buf, i);
      }
      final byte[] handle = buf.getString(); 

      if (mode == RESUME || mode == APPEND) {
        offset += skip;
      }

      final long[] _offset = new long[1];
      _offset[0] = offset;
      OutputStream out = new OutputStream() {
        private boolean init = true;
        private boolean isClosed = false;
        private int[] ackid = new int[1];
        private int startid = 0;
        private int _ackid = 0;
        private int ackcount = 0;
        private int writecount = 0;
        private Header header = new Header();

        @Override
        public void write(byte[] d) throws IOException {
          write(d, 0, d.length);
        }

        @Override
        public void write(byte[] d, int s, int len) throws IOException {
          if (init) {
            startid = seq;
            _ackid = seq;
            init = false;
          }

          if (isClosed) {
            throw new IOException("stream already closed");
          }

          try {
            int _len = len;
            while (_len > 0) {
              if (useWriteFlushWorkaround && rwsize < 21 + handle.length + _len + 4) {
                flush();
              }
              int sent = sendWRITE(handle, _offset[0], d, s, _len);
              writecount++;
              _offset[0] += sent;
              s += sent;
              _len -= sent;
              if ((seq - 1) == startid || io_in.available() >= 1024) {
                while (io_in.available() > 0) {
                  if (checkStatus(ackid, header)) {
                    _ackid = ackid[0];
                    if (startid > _ackid || _ackid > seq - 1) {
                      throw new SftpException(SSH_FX_FAILURE, "");
                    }
                    ackcount++;
                  } else {
                    break;
                  }
                }
              }
            }
            if (monitor != null && !monitor.count(len)) {
              close();
              throw new IOException("canceled");
            }
          } catch (IOException e) {
            throw e;
          } catch (Exception e) {
            throw new IOException(e.toString(), e);
          }
        }

        byte[] _data = new byte[1];

        @Override
        public void write(int foo) throws IOException {
          _data[0] = (byte) foo;
          write(_data, 0, 1);
        }

        @Override
        public void flush() throws IOException {

          if (isClosed) {
            throw new IOException("stream already closed");
          }

          if (!init) {
            try {
              while (writecount > ackcount) {
                if (!checkStatus(null, header)) {
                  break;
                }
                ackcount++;
              }
            } catch (SftpException e) {
              throw new IOException(e.toString(), e);
            }
          }
        }

        @Override
        public void close() throws IOException {
          if (isClosed) {
            return;
          }
          flush();
          if (monitor != null)
            monitor.end();
          try {
            _sendCLOSE(handle, header);
          } catch (IOException e) {
            throw e;
          } catch (Exception e) {
            throw new IOException(e.toString(), e);
          }
          isClosed = true;
        }
      };
      return out;
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void get(String src, String dst) throws SftpException {
    get(src, dst, null, OVERWRITE);
  }

  public void get(String src, String dst, SftpProgressMonitor monitor) throws SftpException {
    get(src, dst, monitor, OVERWRITE);
  }

  public void get(String src, String dst, SftpProgressMonitor monitor, int mode)
      throws SftpException {
    
    boolean _dstExist = false;
    String _dst = null;
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      src = remoteAbsolutePath(src);
      dst = localAbsolutePath(dst);

      Vector<String> v = glob_remote(src);
      int vsize = v.size();
      if (vsize == 0) {
        throw new SftpException(SSH_FX_NO_SUCH_FILE, "No such file");
      }

      File dstFile = new File(dst);
      boolean isDstDir = dstFile.isDirectory();
      StringBuilder dstsb = null;
      if (isDstDir) {
        if (!dst.endsWith(file_separator)) {
          dst += file_separator;
        }
        dstsb = new StringBuilder(dst);
      } else if (vsize > 1) {
        throw new SftpException(SSH_FX_FAILURE,
            "Copying multiple files, but destination is missing or a file.");
      }

      for (int j = 0; j < vsize; j++) {
        String _src = v.elementAt(j);
        SftpATTRS attr = _stat(_src);
        if (attr.isDir()) {
          throw new SftpException(SSH_FX_FAILURE, "not supported to get directory " + _src);
        }

        _dst = null;
        if (isDstDir) {
          int i = _src.lastIndexOf('/');
          if (i == -1)
            dstsb.append(_src);
          else
            dstsb.append(_src.substring(i + 1));
          _dst = dstsb.toString();
          if (_dst.indexOf("..") != -1) {
            String dstc = (new File(dst)).getCanonicalPath();
            String _dstc = (new File(_dst)).getCanonicalPath();
            if (!(_dstc.length() > dstc.length()
                && _dstc.substring(0, dstc.length() + 1).equals(dstc + file_separator))) {
              throw new SftpException(SSH_FX_FAILURE, "writing to an unexpected file " + _src);
            }
          }
          dstsb.delete(dst.length(), _dst.length());
        } else {
          _dst = dst;
        }

        File _dstFile = new File(_dst);
        if (mode == RESUME) {
          long size_of_src = attr.getSize();
          long size_of_dst = _dstFile.length();
          if (size_of_dst > size_of_src) {
            throw new SftpException(SSH_FX_FAILURE, "failed to resume for " + _dst);
          }
          if (size_of_dst == size_of_src) {
            return;
          }
        }

        if (monitor != null) {
          monitor.init(SftpProgressMonitor.GET, _src, _dst, attr.getSize());
          if (mode == RESUME) {
            monitor.count(_dstFile.length());
          }
        }

        _dstExist = _dstFile.exists();
        try (OutputStream fos = mode == OVERWRITE ? new FileOutputStream(_dst)
            : new FileOutputStream(_dst, true) ) {
          
          _get(_src, fos, monitor, mode, new File(_dst).length());
        }
      }
    } catch (Exception e) {
      if (!_dstExist && _dst != null) {
        File _dstFile = new File(_dst);
        if (_dstFile.exists() && _dstFile.length() == 0) {
          _dstFile.delete();
        }
      }
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void get(String src, OutputStream dst) throws SftpException {
    get(src, dst, null, OVERWRITE, 0);
  }

  public void get(String src, OutputStream dst, SftpProgressMonitor monitor) throws SftpException {
    get(src, dst, monitor, OVERWRITE, 0);
  }

  public void get(String src, OutputStream dst, SftpProgressMonitor monitor, int mode, long skip)
      throws SftpException {
    
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      src = remoteAbsolutePath(src);
      src = isUnique(src);

      if (monitor != null) {
        SftpATTRS attr = _stat(src);
        monitor.init(SftpProgressMonitor.GET, src, "??", attr.getSize());
        if (mode == RESUME) {
          monitor.count(skip);
        }
      }
      _get(src, dst, monitor, mode, skip);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  private void _get(String src, OutputStream dst, SftpProgressMonitor monitor, int mode, long skip)
      throws SftpException {
    
    byte[] srcb = Util.str2byte(src, fEncoding);
    try {
      sendOPENR(srcb);

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS && type != SSH_FXP_HANDLE) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }

      if (type == SSH_FXP_STATUS) {
        int i = buf.getInt();
        throwStatusError(buf, i);
      }

      byte[] handle = buf.getString(); 

      long offset = 0;
      if (mode == RESUME) {
        offset += skip;
      }

      int request_max = 1;
      rq.init();
      long request_offset = offset;

      int request_len = buf.buffer.length - 13;
      if (server_version == 0) {
        request_len = 1024;
      }

      loop: while (true) {

        while (rq.count() < request_max) {
          sendREAD(handle, request_offset, request_len, rq);
          request_offset += request_len;
        }

        header = header(buf, header);
        length = header.length;
        type = header.type;

        RequestQueue.Request rr = null;
        try {
          rr = rq.get(header.rid);
        } catch (RequestQueue.OutOfOrderException e) {
          request_offset = e.offset;
          skip(header.length);
          rq.cancel(header, buf);
          continue;
        }

        if (type == SSH_FXP_STATUS) {
          fill(buf, length);
          int i = buf.getInt();
          if (i == SSH_FX_EOF) {
            break loop;
          }
          throwStatusError(buf, i);
        }

        if (type != SSH_FXP_DATA) {
          break loop;
        }

        buf.rewind();
        fill(buf.buffer, 0, 4);
        length -= 4;
        int length_of_data = buf.getInt(); 

        int optional_data = length - length_of_data;

        int foo = length_of_data;
        while (foo > 0) {
          int bar = foo;
          if (bar > buf.buffer.length) {
            bar = buf.buffer.length;
          }
          int data_len = io_in.read(buf.buffer, 0, bar);
          if (data_len < 0) {
            break loop;
          }

          dst.write(buf.buffer, 0, data_len);

          offset += data_len;
          foo -= data_len;

          if (monitor != null) {
            if (!monitor.count(data_len)) {
              skip(foo);
              if (optional_data > 0) {
                skip(optional_data);
              }
              break loop;
            }
          }
        }
        
        if (optional_data > 0) {
          skip(optional_data);
        }

        if (length_of_data < rr.length) { 
          rq.cancel(header, buf);
          sendREAD(handle, rr.offset + length_of_data, (int) (rr.length - length_of_data), rq);
          request_offset = rr.offset + rr.length;
        }

        if (request_max < rq.size()) {
          request_max++;
        }
      }
      dst.flush();

      if (monitor != null)
        monitor.end();

      rq.cancel(header, buf);

      _sendCLOSE(handle, header);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  private class RequestQueue {
    class OutOfOrderException extends Exception {
      private static final long serialVersionUID = -1L;
      long offset;

      OutOfOrderException(long offset) {
        this.offset = offset;
      }
    }

    class Request {
      int id;
      long offset;
      long length;
    }

    Request[] rrq = null;
    int head, count;

    RequestQueue(int size) {
      rrq = new Request[size];
      for (int i = 0; i < rrq.length; i++) {
        rrq[i] = new Request();
      }
      init();
    }

    void init() {
      head = count = 0;
    }

    void add(int id, long offset, int length) {
      if (count == 0)
        head = 0;
      int tail = head + count;
      if (tail >= rrq.length)
        tail -= rrq.length;
      rrq[tail].id = id;
      rrq[tail].offset = offset;
      rrq[tail].length = length;
      count++;
    }

    Request get(int id) throws OutOfOrderException, SftpException {
      count -= 1;
      int i = head;
      head++;
      if (head == rrq.length)
        head = 0;
      if (rrq[i].id != id) {
        long offset = getOffset();
        boolean find = false;
        for (int j = 0; j < rrq.length; j++) {
          if (rrq[j].id == id) {
            find = true;
            rrq[j].id = 0;
            break;
          }
        }
        if (find)
          throw new OutOfOrderException(offset);
        throw new SftpException(SSH_FX_FAILURE, "RequestQueue: unknown request id " + id);
      }
      rrq[i].id = 0;
      return rrq[i];
    }

    int count() {
      return count;
    }

    int size() {
      return rrq.length;
    }

    void cancel(Header header, Buffer buf) throws IOException {
      int _count = count;
      for (int i = 0; i < _count; i++) {
        header = header(buf, header);
        int length = header.length;
        for (int j = 0; j < rrq.length; j++) {
          if (rrq[j].id == header.rid) {
            rrq[j].id = 0;
            break;
          }
        }
        skip(length);
      }
      init();
    }

    long getOffset() {
      long result = Long.MAX_VALUE;

      for (int i = 0; i < rrq.length; i++) {
        if (rrq[i].id == 0)
          continue;
        if (result > rrq[i].offset)
          result = rrq[i].offset;
      }

      return result;
    }
  }

  public InputStream get(String src) throws SftpException {
    return get(src, null, 0L);
  }

  public InputStream get(String src, SftpProgressMonitor monitor) throws SftpException {
    return get(src, monitor, 0L);
  }

  @Deprecated
  public InputStream get(String src, int mode) throws SftpException {
    return get(src, null, 0L);
  }

  @Deprecated
  public InputStream get(String src, final SftpProgressMonitor monitor, final int mode)
      throws SftpException {
    return get(src, monitor, 0L);
  }

  public InputStream get(String src, final SftpProgressMonitor monitor, final long skip)
      throws SftpException {

    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      src = remoteAbsolutePath(src);
      src = isUnique(src);

      byte[] srcb = Util.str2byte(src, fEncoding);

      SftpATTRS attr = _stat(srcb);
      if (monitor != null) {
        monitor.init(SftpProgressMonitor.GET, src, "??", attr.getSize());
      }

      sendOPENR(srcb);

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS && type != SSH_FXP_HANDLE) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      if (type == SSH_FXP_STATUS) {
        int i = buf.getInt();
        throwStatusError(buf, i);
      }

      final byte[] handle = buf.getString(); 

      rq.init();

      InputStream in = new InputStream() {
        long offset = skip;
        boolean closed = false;
        int rest_length = 0;
        byte[] _data = new byte[1];
        byte[] rest_byte = new byte[1024];
        Header header = new Header();
        int request_max = 1;
        long request_offset = offset;

        @Override
        public int read() throws IOException {
          if (closed)
            return -1;
          int i = read(_data, 0, 1);
          if (i == -1) {
            return -1;
          } else {
            return _data[0] & 0xff;
          }
        }

        @Override
        public int read(byte[] d) throws IOException {
          if (closed)
            return -1;
          return read(d, 0, d.length);
        }

        @Override
        public int read(byte[] d, int s, int len) throws IOException {
          if (closed)
            return -1;
          if (d == null) {
            throw new NullPointerException();
          }
          if (s < 0 || len < 0 || s + len > d.length) {
            throw new IndexOutOfBoundsException();
          }
          if (len == 0) {
            return 0;
          }

          if (rest_length > 0) {
            int foo = rest_length;
            if (foo > len)
              foo = len;
            System.arraycopy(rest_byte, 0, d, s, foo);
            if (foo != rest_length) {
              System.arraycopy(rest_byte, foo, rest_byte, 0, rest_length - foo);
            }

            if (monitor != null) {
              if (!monitor.count(foo)) {
                close();
                return -1;
              }
            }

            rest_length -= foo;
            return foo;
          }

          if (buf.buffer.length - 13 < len) {
            len = buf.buffer.length - 13;
          }
          if (server_version == 0 && len > 1024) {
            len = 1024;
          }

          if (rq.count() == 0 || true 
                                      
          ) {
            int request_len = buf.buffer.length - 13;
            if (server_version == 0) {
              request_len = 1024;
            }

            while (rq.count() < request_max) {
              try {
                sendREAD(handle, request_offset, request_len, rq);
              } catch (Exception e) {
                throw new IOException("error");
              }
              request_offset += request_len;
            }
          }

          header = header(buf, header);
          rest_length = header.length;
          int type = header.type;
          int id = header.rid;

          RequestQueue.Request rr = null;
          try {
            rr = rq.get(header.rid);
          } catch (RequestQueue.OutOfOrderException e) {
            request_offset = e.offset;
            skip(header.length);
            rq.cancel(header, buf);
            return 0;
          } catch (SftpException e) {
            throw new IOException("error: " + e.toString(), e);
          }

          if (type != SSH_FXP_STATUS && type != SSH_FXP_DATA) {
            throw new IOException("error");
          }
          if (type == SSH_FXP_STATUS) {
            fill(buf, rest_length);
            int i = buf.getInt();
            rest_length = 0;
            if (i == SSH_FX_EOF) {
              close();
              return -1;
            }
            
            throw new IOException("error");
          }

          buf.rewind();
          fill(buf.buffer, 0, 4);
          int length_of_data = buf.getInt();
          rest_length -= 4;

          int optional_data = rest_length - length_of_data;

          offset += length_of_data;
          int foo = length_of_data;
          if (foo > 0) {
            int bar = foo;
            if (bar > len) {
              bar = len;
            }
            int i = io_in.read(d, s, bar);
            if (i < 0) {
              return -1;
            }
            foo -= i;
            rest_length = foo;

            if (foo > 0) {
              if (rest_byte.length < foo) {
                rest_byte = new byte[foo];
              }
              int _s = 0;
              int _len = foo;
              int j;
              while (_len > 0) {
                j = io_in.read(rest_byte, _s, _len);
                if (j <= 0)
                  break;
                _s += j;
                _len -= j;
              }
            }

            if (optional_data > 0) {
              io_in.skip(optional_data);
            }

            if (length_of_data < rr.length) { 
              rq.cancel(header, buf);
              try {
                sendREAD(handle, rr.offset + length_of_data, (int) (rr.length - length_of_data),
                    rq);
              } catch (Exception e) {
                throw new IOException("error");
              }
              request_offset = rr.offset + rr.length;
            }

            if (request_max < rq.size()) {
              request_max++;
            }

            if (monitor != null) {
              if (!monitor.count(i)) {
                close();
                return -1;
              }
            }

            return i;
          }
          return 0; 
        }

        @Override
        public void close() throws IOException {
          if (closed)
            return;
          closed = true;
          if (monitor != null)
            monitor.end();
          rq.cancel(header, buf);
          try {
            _sendCLOSE(handle, header);
          } catch (IOException e) {
            throw e;
          } catch (Exception e) {
            throw new IOException(e.toString(), e);
          }
        }
      };
      return in;
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public Vector<LsEntry> ls(String path) throws SftpException {
    final Vector<LsEntry> v = new Vector<>();
    LsEntrySelector selector = new LsEntrySelector() {
      @Override
      public int select(LsEntry entry) {
        v.addElement(entry);
        return CONTINUE;
      }
    };
    ls(path, selector);
    return v;
  }

  public void ls(String path, LsEntrySelector selector) throws SftpException {
    
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);
      byte[] pattern = null;
      Vector<LsEntry> v = new Vector<>();

      int foo = path.lastIndexOf('/');
      String dir = path.substring(0, ((foo == 0) ? 1 : foo));
      String _pattern = path.substring(foo + 1);
      dir = Util.unquote(dir);

      byte[][] _pattern_utf8 = new byte[1][];
      boolean pattern_has_wildcard = isPattern(_pattern, _pattern_utf8);

      if (pattern_has_wildcard) {
        pattern = _pattern_utf8[0];
      } else {
        String upath = Util.unquote(path);
        
        SftpATTRS attr = _stat(upath);
        if (attr.isDir()) {
          pattern = null;
          dir = upath;
        } else {
          
          if (fEncoding_is_utf8) {
            pattern = _pattern_utf8[0];
            pattern = Util.unquote(pattern);
          } else {
            _pattern = Util.unquote(_pattern);
            pattern = Util.str2byte(_pattern, fEncoding);
          }
        }
      }

      sendOPENDIR(Util.str2byte(dir, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS && type != SSH_FXP_HANDLE) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      if (type == SSH_FXP_STATUS) {
        int i = buf.getInt();
        throwStatusError(buf, i);
      }

      int cancel = LsEntrySelector.CONTINUE;
      byte[] handle = buf.getString(); 

      while (cancel == LsEntrySelector.CONTINUE) {

        sendREADDIR(handle);

        header = header(buf, header);
        length = header.length;
        type = header.type;
        if (type != SSH_FXP_STATUS && type != SSH_FXP_NAME) {
          throw new SftpException(SSH_FX_FAILURE, "");
        }
        if (type == SSH_FXP_STATUS) {
          fill(buf, length);
          int i = buf.getInt();
          if (i == SSH_FX_EOF)
            break;
          throwStatusError(buf, i);
        }

        buf.rewind();
        fill(buf.buffer, 0, 4);
        length -= 4;
        int count = buf.getInt();

        byte[] str;
        int flags;

        buf.reset();
        while (count > 0) {
          if (length > 0) {
            buf.shift();
            int j = (buf.buffer.length > (buf.index + length)) ? length
                : (buf.buffer.length - buf.index);
            int i = fill(buf.buffer, buf.index, j);
            buf.index += i;
            length -= i;
          }
          byte[] filename = buf.getString();
          byte[] longname = null;
          if (server_version <= 3) {
            longname = buf.getString();
          }
          SftpATTRS attrs = SftpATTRS.getATTR(buf);

          if (cancel == LsEntrySelector.BREAK) {
            count--;
            continue;
          }

          boolean find = false;
          String f = null;
          if (pattern == null) {
            find = true;
          } else if (!pattern_has_wildcard) {
            find = Util.array_equals(pattern, filename);
          } else {
            byte[] _filename = filename;
            if (!fEncoding_is_utf8) {
              f = Util.byte2str(_filename, fEncoding);
              _filename = Util.str2byte(f, StandardCharsets.UTF_8);
            }
            find = Util.glob(pattern, _filename);
          }

          if (find) {
            if (f == null) {
              f = Util.byte2str(filename, fEncoding);
            }
            String l = null;
            if (longname == null) {
              
              l = attrs.toString() + " " + f;
            } else {
              l = Util.byte2str(longname, fEncoding);
            }

            cancel = selector.select(new LsEntry(f, l, attrs));
          }

          count--;
        }
      }
      _sendCLOSE(handle, header);

    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public String readlink(String path) throws SftpException {
    try {
      if (server_version < 3) {
        throw new SftpException(SSH_FX_OP_UNSUPPORTED,
            "The remote sshd is too old to support symlink operation.");
      }

      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      path = isUnique(path);

      sendREADLINK(Util.str2byte(path, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS && type != SSH_FXP_NAME) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      if (type == SSH_FXP_NAME) {
        int count = buf.getInt(); 
        byte[] filename = null;
        for (int i = 0; i < count; i++) {
          filename = buf.getString();
          if (server_version <= 3) {
            byte[] longname = buf.getString();
          }
          SftpATTRS.getATTR(buf);
        }
        return Util.byte2str(filename, fEncoding);
      }

      int i = buf.getInt();
      throwStatusError(buf, i);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
    return null;
  }

  public void symlink(String oldpath, String newpath) throws SftpException {
    if (server_version < 3) {
      throw new SftpException(SSH_FX_OP_UNSUPPORTED,
          "The remote sshd is too old to support symlink operation.");
    }

    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      String _oldpath = remoteAbsolutePath(oldpath);
      newpath = remoteAbsolutePath(newpath);

      _oldpath = isUnique(_oldpath);
      if (oldpath.charAt(0) != '/') { 
        String cwd = getCwd();
        oldpath = _oldpath.substring(cwd.length() + (cwd.endsWith("/") ? 0 : 1));
      } else {
        oldpath = _oldpath;
      }

      if (isPattern(newpath)) {
        throw new SftpException(SSH_FX_FAILURE, newpath);
      }
      newpath = Util.unquote(newpath);

      sendSYMLINK(Util.str2byte(oldpath, fEncoding), Util.str2byte(newpath, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }

      int i = buf.getInt();
      if (i == SSH_FX_OK)
        return;
      throwStatusError(buf, i);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void hardlink(String oldpath, String newpath) throws SftpException {
    if (!extension_hardlink) {
      throw new SftpException(SSH_FX_OP_UNSUPPORTED, "hardlink@openssh.com is not supported");
    }

    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      String _oldpath = remoteAbsolutePath(oldpath);
      newpath = remoteAbsolutePath(newpath);

      _oldpath = isUnique(_oldpath);
      if (oldpath.charAt(0) != '/') { 
        String cwd = getCwd();
        oldpath = _oldpath.substring(cwd.length() + (cwd.endsWith("/") ? 0 : 1));
      } else {
        oldpath = _oldpath;
      }

      if (isPattern(newpath)) {
        throw new SftpException(SSH_FX_FAILURE, newpath);
      }
      newpath = Util.unquote(newpath);

      sendHARDLINK(Util.str2byte(oldpath, fEncoding), Util.str2byte(newpath, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }

      int i = buf.getInt();
      if (i == SSH_FX_OK)
        return;
      throwStatusError(buf, i);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void rename(String oldpath, String newpath) throws SftpException {
    if (server_version < 2) {
      throw new SftpException(SSH_FX_OP_UNSUPPORTED,
          "The remote sshd is too old to support rename operation.");
    }

    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      oldpath = remoteAbsolutePath(oldpath);
      newpath = remoteAbsolutePath(newpath);

      oldpath = isUnique(oldpath);

      Vector<String> v = glob_remote(newpath);
      int vsize = v.size();
      if (vsize >= 2) {
        throw new SftpException(SSH_FX_FAILURE, v.toString());
      }
      if (vsize == 1) {
        newpath = v.elementAt(0);
      } else { 
        if (isPattern(newpath))
          throw new SftpException(SSH_FX_FAILURE, newpath);
        newpath = Util.unquote(newpath);
      }

      sendRENAME(Util.str2byte(oldpath, fEncoding), Util.str2byte(newpath, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }

      int i = buf.getInt();
      if (i == SSH_FX_OK)
        return;
      throwStatusError(buf, i);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void rm(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();

      Header header = new Header();

      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);
        sendREMOVE(Util.str2byte(path, fEncoding));

        header = header(buf, header);
        int length = header.length;
        int type = header.type;

        fill(buf, length);

        if (type != SSH_FXP_STATUS) {
          throw new SftpException(SSH_FX_FAILURE, "");
        }
        int i = buf.getInt();
        if (i != SSH_FX_OK) {
          throwStatusError(buf, i);
        }
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  private boolean isRemoteDir(String path) {
    try {
      sendSTAT(Util.str2byte(path, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_ATTRS) {
        return false;
      }
      SftpATTRS attr = SftpATTRS.getATTR(buf);
      return attr.isDir();
    } catch (Exception e) {
    }
    return false;
  }

  public void chgrp(int gid, String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();
      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);

        SftpATTRS attr = _stat(path);

        attr.setFLAGS(0);
        attr.setUIDGID(attr.uid, gid);
        _setStat(path, attr);
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void chown(int uid, String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();
      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);

        SftpATTRS attr = _stat(path);

        attr.setFLAGS(0);
        attr.setUIDGID(uid, attr.gid);
        _setStat(path, attr);
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void chmod(int permissions, String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();
      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);

        SftpATTRS attr = _stat(path);

        attr.setFLAGS(0);
        attr.setPERMISSIONS(permissions);
        _setStat(path, attr);
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void setMtime(String path, int mtime) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();
      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);

        SftpATTRS attr = _stat(path);

        attr.setFLAGS(0);
        attr.setACMODTIME(attr.getATime(), mtime);
        _setStat(path, attr);
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void rmdir(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();

      Header header = new Header();

      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);
        sendRMDIR(Util.str2byte(path, fEncoding));

        header = header(buf, header);
        int length = header.length;
        int type = header.type;

        fill(buf, length);

        if (type != SSH_FXP_STATUS) {
          throw new SftpException(SSH_FX_FAILURE, "");
        }

        int i = buf.getInt();
        if (i != SSH_FX_OK) {
          throwStatusError(buf, i);
        }
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void mkdir(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      sendMKDIR(Util.str2byte(path, fEncoding), null);

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }

      int i = buf.getInt();
      if (i == SSH_FX_OK)
        return;
      throwStatusError(buf, i);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public SftpATTRS stat(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);
      path = isUnique(path);

      return _stat(path);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
    
  }

  private SftpATTRS _stat(byte[] path) throws SftpException {
    try {

      sendSTAT(path);

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_ATTRS) {
        if (type == SSH_FXP_STATUS) {
          int i = buf.getInt();
          throwStatusError(buf, i);
        }
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      SftpATTRS attr = SftpATTRS.getATTR(buf);
      return attr;
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
    
  }

  private SftpATTRS _stat(String path) throws SftpException {
    return _stat(Util.str2byte(path, fEncoding));
  }

  public SftpStatVFS statVFS(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);
      path = isUnique(path);

      return _statVFS(path);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
    
  }

  private SftpStatVFS _statVFS(byte[] path) throws SftpException {
    if (!extension_statvfs) {
      throw new SftpException(SSH_FX_OP_UNSUPPORTED, "statvfs@openssh.com is not supported");
    }

    try {

      sendSTATVFS(path);

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != (SSH_FXP_EXTENDED_REPLY & 0xff)) {
        if (type == SSH_FXP_STATUS) {
          int i = buf.getInt();
          throwStatusError(buf, i);
        }
        throw new SftpException(SSH_FX_FAILURE, "");
      } else {
        SftpStatVFS stat = SftpStatVFS.getStatVFS(buf);
        return stat;
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
    
  }

  private SftpStatVFS _statVFS(String path) throws SftpException {
    return _statVFS(Util.str2byte(path, fEncoding));
  }

  public SftpATTRS lstat(String path) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);
      path = isUnique(path);

      return _lstat(path);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  private SftpATTRS _lstat(String path) throws SftpException {
    try {
      sendLSTAT(Util.str2byte(path, fEncoding));

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_ATTRS) {
        if (type == SSH_FXP_STATUS) {
          int i = buf.getInt();
          throwStatusError(buf, i);
        }
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      SftpATTRS attr = SftpATTRS.getATTR(buf);
      return attr;
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  private byte[] _realpath(String path) throws SftpException, IOException, Exception {
    sendREALPATH(Util.str2byte(path, fEncoding));

    Header header = new Header();
    header = header(buf, header);
    int length = header.length;
    int type = header.type;

    fill(buf, length);

    if (type != SSH_FXP_STATUS && type != SSH_FXP_NAME) {
      throw new SftpException(SSH_FX_FAILURE, "");
    }
    int i;
    if (type == SSH_FXP_STATUS) {
      i = buf.getInt();
      throwStatusError(buf, i);
    }
    i = buf.getInt(); 

    byte[] str = null;
    while (i-- > 0) {
      str = buf.getString(); 
      if (server_version <= 3) {
        byte[] lname = buf.getString(); 
      }
      SftpATTRS attr = SftpATTRS.getATTR(buf); 
    }
    return str;
  }

  public void setStat(String path, SftpATTRS attr) throws SftpException {
    try {
      ((MyPipedInputStream) io_in).updateReadSide();

      path = remoteAbsolutePath(path);

      Vector<String> v = glob_remote(path);
      int vsize = v.size();
      for (int j = 0; j < vsize; j++) {
        path = v.elementAt(j);
        _setStat(path, attr);
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  private void _setStat(String path, SftpATTRS attr) throws SftpException {
    try {
      sendSETSTAT(Util.str2byte(path, fEncoding), attr);

      Header header = new Header();
      header = header(buf, header);
      int length = header.length;
      int type = header.type;

      fill(buf, length);

      if (type != SSH_FXP_STATUS) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      int i = buf.getInt();
      if (i != SSH_FX_OK) {
        throwStatusError(buf, i);
      }
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public String pwd() throws SftpException {
    return getCwd();
  }

  public String lpwd() {
    return lcwd;
  }

  public String version() {
    return version;
  }

  public String getHome() throws SftpException {
    if (home == null) {
      try {
        ((MyPipedInputStream) io_in).updateReadSide();

        byte[] _home = _realpath("");
        home = Util.byte2str(_home, fEncoding);
      } catch (Exception e) {
        if (e instanceof SftpException)
          throw (SftpException) e;
        throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
      }
    }
    return home;
  }

  private String getCwd() throws SftpException {
    if (cwd == null)
      cwd = getHome();
    return cwd;
  }

  private void setCwd(String cwd) {
    this.cwd = cwd;
  }

  private void read(byte[] buf, int s, int l) throws IOException, SftpException {
    int i = 0;
    while (l > 0) {
      i = io_in.read(buf, s, l);
      if (i <= 0) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      s += i;
      l -= i;
    }
  }

  private boolean checkStatus(int[] ackid, Header header) throws IOException, SftpException {
    header = header(buf, header);
    int length = header.length;
    int type = header.type;
    if (ackid != null)
      ackid[0] = header.rid;

    fill(buf, length);

    if (type != SSH_FXP_STATUS) {
      throw new SftpException(SSH_FX_FAILURE, "");
    }
    int i = buf.getInt();
    if (i != SSH_FX_OK) {
      throwStatusError(buf, i);
    }
    return true;
  }

  private boolean _sendCLOSE(byte[] handle, Header header) throws Exception {
    sendCLOSE(handle);
    return checkStatus(null, header);
  }

  private void sendINIT() throws Exception {
    packet.reset();
    putHEAD(SSH_FXP_INIT, 5);
    buf.putInt(3); 
    getSession().write(packet, this, 5 + 4);
  }

  private void sendREALPATH(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_REALPATH, path);
  }

  private void sendSTAT(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_STAT, path);
  }

  private void sendSTATVFS(byte[] path) throws Exception {
    sendPacketPath((byte) 0, path, "statvfs@openssh.com");
  }

  private void sendLSTAT(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_LSTAT, path);
  }

  private void sendFSTAT(byte[] handle) throws Exception {
    sendPacketPath(SSH_FXP_FSTAT, handle);
  }

  private void sendSETSTAT(byte[] path, SftpATTRS attr) throws Exception {
    packet.reset();
    putHEAD(SSH_FXP_SETSTAT, 9 + path.length + attr.length());
    buf.putInt(seq++);
    buf.putString(path); 
    attr.dump(buf);
    getSession().write(packet, this, 9 + path.length + attr.length() + 4);
  }

  private void sendREMOVE(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_REMOVE, path);
  }

  private void sendMKDIR(byte[] path, SftpATTRS attr) throws Exception {
    packet.reset();
    putHEAD(SSH_FXP_MKDIR, 9 + path.length + (attr != null ? attr.length() : 4));
    buf.putInt(seq++);
    buf.putString(path); 
    if (attr != null)
      attr.dump(buf);
    else
      buf.putInt(0);
    getSession().write(packet, this, 9 + path.length + (attr != null ? attr.length() : 4) + 4);
  }

  private void sendRMDIR(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_RMDIR, path);
  }

  private void sendSYMLINK(byte[] p1, byte[] p2) throws Exception {
    sendPacketPath(SSH_FXP_SYMLINK, p1, p2);
  }

  private void sendHARDLINK(byte[] p1, byte[] p2) throws Exception {
    sendPacketPath((byte) 0, p1, p2, "hardlink@openssh.com");
  }

  private void sendREADLINK(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_READLINK, path);
  }

  private void sendOPENDIR(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_OPENDIR, path);
  }

  private void sendREADDIR(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_READDIR, path);
  }

  private void sendRENAME(byte[] p1, byte[] p2) throws Exception {
    sendPacketPath(SSH_FXP_RENAME, p1, p2,
        extension_posix_rename ? "posix-rename@openssh.com" : null);
  }

  private void sendCLOSE(byte[] path) throws Exception {
    sendPacketPath(SSH_FXP_CLOSE, path);
  }

  private void sendOPENR(byte[] path) throws Exception {
    sendOPEN(path, SSH_FXF_READ);
  }

  private void sendOPENW(byte[] path) throws Exception {
    sendOPEN(path, SSH_FXF_WRITE | SSH_FXF_CREAT | SSH_FXF_TRUNC);
  }

  private void sendOPENA(byte[] path) throws Exception {
    sendOPEN(path, SSH_FXF_WRITE |  SSH_FXF_CREAT);
  }

  private void sendOPEN(byte[] path, int mode) throws Exception {
    packet.reset();
    putHEAD(SSH_FXP_OPEN, 17 + path.length);
    buf.putInt(seq++);
    buf.putString(path);
    buf.putInt(mode);
    buf.putInt(0); 
    getSession().write(packet, this, 17 + path.length + 4);
  }

  private void sendPacketPath(byte fxp, byte[] path) throws Exception {
    sendPacketPath(fxp, path, (String) null);
  }

  private void sendPacketPath(byte fxp, byte[] path, String extension) throws Exception {
    packet.reset();
    int len = 9 + path.length;
    if (extension == null) {
      putHEAD(fxp, len);
      buf.putInt(seq++);
    } else {
      len += (4 + extension.length());
      putHEAD(SSH_FXP_EXTENDED, len);
      buf.putInt(seq++);
      buf.putString(Util.str2byte(extension));
    }
    buf.putString(path); 
    getSession().write(packet, this, len + 4);
  }

  private void sendPacketPath(byte fxp, byte[] p1, byte[] p2) throws Exception {
    sendPacketPath(fxp, p1, p2, null);
  }

  private void sendPacketPath(byte fxp, byte[] p1, byte[] p2, String extension) throws Exception {
    packet.reset();
    int len = 13 + p1.length + p2.length;
    if (extension == null) {
      putHEAD(fxp, len);
      buf.putInt(seq++);
    } else {
      len += (4 + extension.length());
      putHEAD(SSH_FXP_EXTENDED, len);
      buf.putInt(seq++);
      buf.putString(Util.str2byte(extension));
    }
    buf.putString(p1);
    buf.putString(p2);
    getSession().write(packet, this, len + 4);
  }

  private int sendWRITE(byte[] handle, long offset, byte[] data, int start, int length)
      throws Exception {
    int _length = length;
    opacket.reset();
    Session _session = getSession();
    int buffer_margin = _session.getBufferMargin();
    if (obuf.buffer.length < obuf.index + 13 + 21 + handle.length + length + buffer_margin) {
      _length = obuf.buffer.length - (obuf.index + 13 + 21 + handle.length + buffer_margin);
      
    }

    putHEAD(obuf, SSH_FXP_WRITE, 21 + handle.length + _length); 
    obuf.putInt(seq++); 
    obuf.putString(handle); 
    obuf.putLong(offset); 
    if (obuf.buffer != data) {
      obuf.putString(data, start, _length); 
    } else {
      obuf.putInt(_length);
      obuf.skip(_length);
    }
    _session.write(opacket, this, 21 + handle.length + _length + 4);
    return _length;
  }

  private void sendREAD(byte[] handle, long offset, int length) throws Exception {
    sendREAD(handle, offset, length, null);
  }

  private void sendREAD(byte[] handle, long offset, int length, RequestQueue rrq) throws Exception {
    packet.reset();
    putHEAD(SSH_FXP_READ, 21 + handle.length);
    buf.putInt(seq++);
    buf.putString(handle);
    buf.putLong(offset);
    buf.putInt(length);
    getSession().write(packet, this, 21 + handle.length + 4);
    if (rrq != null) {
      rrq.add(seq - 1, offset, length);
    }
  }

  private void putHEAD(Buffer buf, byte type, int length) throws Exception {
    buf.putByte((byte) Session.SSH_MSG_CHANNEL_DATA);
    buf.putInt(recipient);
    buf.putInt(length + 4);
    buf.putInt(length);
    buf.putByte(type);
  }

  private void putHEAD(byte type, int length) throws Exception {
    putHEAD(buf, type, length);
  }

  private Vector<String> glob_remote(String _path) throws Exception {
    Vector<String> v = new Vector<>();
    int i = 0;

    int foo = _path.lastIndexOf('/');
    if (foo < 0) { 
      v.addElement(Util.unquote(_path));
      return v;
    }

    String dir = _path.substring(0, ((foo == 0) ? 1 : foo));
    String _pattern = _path.substring(foo + 1);

    dir = Util.unquote(dir);

    byte[] pattern = null;
    byte[][] _pattern_utf8 = new byte[1][];
    boolean pattern_has_wildcard = isPattern(_pattern, _pattern_utf8);

    if (!pattern_has_wildcard) {
      if (!dir.equals("/"))
        dir += "/";
      v.addElement(dir + Util.unquote(_pattern));
      return v;
    }

    pattern = _pattern_utf8[0];

    sendOPENDIR(Util.str2byte(dir, fEncoding));

    Header header = new Header();
    header = header(buf, header);
    int length = header.length;
    int type = header.type;

    fill(buf, length);

    if (type != SSH_FXP_STATUS && type != SSH_FXP_HANDLE) {
      throw new SftpException(SSH_FX_FAILURE, "");
    }
    if (type == SSH_FXP_STATUS) {
      i = buf.getInt();
      throwStatusError(buf, i);
    }

    byte[] handle = buf.getString(); 
    String pdir = null; 

    while (true) {
      sendREADDIR(handle);
      header = header(buf, header);
      length = header.length;
      type = header.type;

      if (type != SSH_FXP_STATUS && type != SSH_FXP_NAME) {
        throw new SftpException(SSH_FX_FAILURE, "");
      }
      if (type == SSH_FXP_STATUS) {
        fill(buf, length);
        break;
      }

      buf.rewind();
      fill(buf.buffer, 0, 4);
      length -= 4;
      int count = buf.getInt();

      byte[] str;
      int flags;

      buf.reset();
      while (count > 0) {
        if (length > 0) {
          buf.shift();
          int j =
              (buf.buffer.length > (buf.index + length)) ? length : (buf.buffer.length - buf.index);
          i = io_in.read(buf.buffer, buf.index, j);
          if (i <= 0)
            break;
          buf.index += i;
          length -= i;
        }

        byte[] filename = buf.getString();
        
        if (server_version <= 3) {
          str = buf.getString(); 
        }
        SftpATTRS attrs = SftpATTRS.getATTR(buf);

        byte[] _filename = filename;
        String f = null;
        boolean found = false;

        if (!fEncoding_is_utf8) {
          f = Util.byte2str(filename, fEncoding);
          _filename = Util.str2byte(f, StandardCharsets.UTF_8);
        }
        found = Util.glob(pattern, _filename);

        if (found) {
          if (f == null) {
            f = Util.byte2str(filename, fEncoding);
          }
          if (pdir == null) {
            pdir = dir;
            if (!pdir.endsWith("/")) {
              pdir += "/";
            }
          }
          v.addElement(pdir + f);
        }
        count--;
      }
    }
    if (_sendCLOSE(handle, header))
      return v;
    return null;
  }

  private boolean isPattern(byte[] path) {
    int length = path.length;
    int i = 0;
    while (i < length) {
      if (path[i] == '*' || path[i] == '?')
        return true;
      if (path[i] == '\\' && (i + 1) < length)
        i++;
      i++;
    }
    return false;
  }

  private Vector<String> glob_local(String _path) throws Exception {
    
    Vector<String> v = new Vector<>();
    byte[] path = Util.str2byte(_path, StandardCharsets.UTF_8);
    int i = path.length - 1;
    while (i >= 0) {
      if (path[i] != '*' && path[i] != '?') {
        i--;
        continue;
      }
      if (!fs_is_bs && i > 0 && path[i - 1] == '\\') {
        i--;
        if (i > 0 && path[i - 1] == '\\') {
          i--;
          i--;
          continue;
        }
      }
      break;
    }

    if (i < 0) {
      v.addElement(fs_is_bs ? _path : Util.unquote(_path));
      return v;
    }

    while (i >= 0) {
      if (path[i] == file_separatorc || (fs_is_bs && path[i] == '/')) { 
                                                                        
        break;
      }
      i--;
    }

    if (i < 0) {
      v.addElement(fs_is_bs ? _path : Util.unquote(_path));
      return v;
    }

    byte[] dir;
    if (i == 0) {
      dir = new byte[] {(byte) file_separatorc};
    } else {
      dir = new byte[i];
      System.arraycopy(path, 0, dir, 0, i);
    }

    byte[] pattern = new byte[path.length - i - 1];
    System.arraycopy(path, i + 1, pattern, 0, pattern.length);

    try {
      String[] children = (new File(Util.byte2str(dir, StandardCharsets.UTF_8))).list();
      String pdir = Util.byte2str(dir) + file_separator;
      for (int j = 0; j < children.length; j++) {
        
        if (Util.glob(pattern, Util.str2byte(children[j], StandardCharsets.UTF_8))) {
          v.addElement(pdir + children[j]);
        }
      }
    } catch (Exception e) {
    }
    return v;
  }

  private void throwStatusError(Buffer buf, int i) throws SftpException {
    if (server_version >= 3 && 
        buf.getLength() >= 4) { 
      byte[] str = buf.getString();
      
      throw new SftpException(i, Util.byte2str(str, StandardCharsets.UTF_8));
    } else {
      throw new SftpException(i, "Failure");
    }
  }

  private static boolean isLocalAbsolutePath(String path) {
    return (new File(path)).isAbsolute();
  }

  @Override
  public void disconnect() {
    super.disconnect();
  }

  private boolean isPattern(String path, byte[][] utf8) {
    byte[] _path = Util.str2byte(path, StandardCharsets.UTF_8);
    if (utf8 != null)
      utf8[0] = _path;
    return isPattern(_path);
  }

  private boolean isPattern(String path) {
    return isPattern(path, null);
  }

  private void fill(Buffer buf, int len) throws IOException {
    buf.reset();
    fill(buf.buffer, 0, len);
    buf.skip(len);
  }

  private int fill(byte[] buf, int s, int len) throws IOException {
    int i = 0;
    int foo = s;
    while (len > 0) {
      i = io_in.read(buf, s, len);
      if (i <= 0) {
        throw new IOException("inputstream is closed");
        
      }
      s += i;
      len -= i;
    }
    return s - foo;
  }

  private void skip(long foo) throws IOException {
    while (foo > 0) {
      long bar = io_in.skip(foo);
      if (bar <= 0)
        break;
      foo -= bar;
    }
  }

  static class Header {
    int length;
    int type;
    int rid;
  }

  private Header header(Buffer buf, Header header) throws IOException {
    buf.rewind();
    int i = fill(buf.buffer, 0, 9);
    header.length = buf.getInt() - 5;
    header.type = buf.getByte() & 0xff;
    header.rid = buf.getInt();
    return header;
  }

  private String remoteAbsolutePath(String path) throws SftpException {
    if (path.charAt(0) == '/')
      return path;
    String cwd = getCwd();
    
    if (cwd.endsWith("/"))
      return cwd + path;
    return cwd + "/" + path;
  }

  private String localAbsolutePath(String path) {
    if (isLocalAbsolutePath(path))
      return path;
    if (lcwd.endsWith(file_separator))
      return lcwd + path;
    return lcwd + file_separator + path;
  }

  private String isUnique(String path) throws SftpException, Exception {
    Vector<String> v = glob_remote(path);
    if (v.size() != 1) {
      throw new SftpException(SSH_FX_FAILURE, path + " is not unique: " + v.toString());
    }
    return v.elementAt(0);
  }

  public int getServerVersion() throws SftpException {
    if (!isConnected()) {
      throw new SftpException(SSH_FX_FAILURE, "The channel is not connected.");
    }
    return server_version;
  }

  @Deprecated
  public void setFilenameEncoding(String encoding) throws SftpException {
    try {
      setFilenameEncoding(Charset.forName(encoding));
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public void setFilenameEncoding(Charset encoding) {
    fEncoding = encoding;
    fEncoding_is_utf8 = fEncoding.equals(StandardCharsets.UTF_8);
  }

  public String getExtension(String key) {
    if (extensions == null)
      return null;
    return extensions.get(key);
  }

  public String realpath(String path) throws SftpException {
    try {
      byte[] _path = _realpath(remoteAbsolutePath(path));
      return Util.byte2str(_path, fEncoding);
    } catch (Exception e) {
      if (e instanceof SftpException)
        throw (SftpException) e;
      throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
    }
  }

  public static class LsEntry implements Comparable<LsEntry> {
    private String filename;
    private String longname;
    private SftpATTRS attrs;

    LsEntry(String filename, String longname, SftpATTRS attrs) {
      setFilename(filename);
      setLongname(longname);
      setAttrs(attrs);
    }

    public String getFilename() {
      return filename;
    };

    void setFilename(String filename) {
      this.filename = filename;
    };

    public String getLongname() {
      return longname;
    };

    void setLongname(String longname) {
      this.longname = longname;
    };

    public SftpATTRS getAttrs() {
      return attrs;
    };

    void setAttrs(SftpATTRS attrs) {
      this.attrs = attrs;
    };

    @Override
    public String toString() {
      return longname;
    }

    @Override
    public int compareTo(LsEntry o) {
      return filename.compareTo(o.getFilename());
    }
  }

  public interface LsEntrySelector {
    public final int CONTINUE = 0;
    public final int BREAK = 1;

    public int select(LsEntry entry);
  }
}