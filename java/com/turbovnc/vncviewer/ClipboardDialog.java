package com.turbovnc.vncviewer;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;
import javax.swing.border.*;

import com.turbovnc.rdr.*;
import com.turbovnc.rfb.*;

class ClipboardDialog extends Dialog implements ActionListener {

  ClipboardDialog(CConn cc_) {
    super(false);
    cc = cc_;

    pt = new JPanel();
    pt.setLayout(new BoxLayout(pt, BoxLayout.LINE_AXIS));
    pt.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    textArea = new JTextArea();
    textArea.setLineWrap(false);
    textArea.setWrapStyleWord(false);
    JScrollPane sp = new JScrollPane(textArea);
    sp.setPreferredSize(new Dimension(300, 250));
    pt.add(sp);

    pb = new JPanel();
    clearButton = new JButton("Clear");
    pb.add(clearButton);
    clearButton.addActionListener(this);
    sendButton = new JButton("Send to VNC server");
    pb.add(sendButton);
    sendButton.addActionListener(this);
    cancelButton = new JButton("Cancel");
    pb.add(cancelButton);
    cancelButton.addActionListener(this);
  }

  protected void populateDialog(JDialog dlg) {
    dlg.setTitle("VNC clipboard");
    dlg.getContentPane().add("Center", pt);
    dlg.getContentPane().add("South", pb);
    dlg.pack();
    dlg.setMinimumSize(dlg.getSize());
  }

  public boolean compareContentsTo(String str) {
    return str.equals(textArea.getText());
  }

  public void setContents(String str) {
    textArea.setText(str);
  }

  public String getContents() {
    return textArea.getText();
  }

  public void serverCutText(String str) {
    setContents(str);
    setClipboard(str);
  }

  public static void setClipboard(String str) {
    try {
      Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (cb != null) {
        StringSelection ss = new StringSelection(str);
        try {
          cb.setContents(ss, null);
        } catch (Exception e) {
          vlog.debug(e.getMessage());
        }
      }
      if (Utils.getBooleanProperty("turbovnc.primary", true)) {
        cb = Toolkit.getDefaultToolkit().getSystemSelection();
        if (cb != null) {
          StringSelection ss = new StringSelection(str);
          try {
            cb.setContents(ss, null);
          } catch (Exception e) {
            vlog.debug(e.getMessage());
          }
        }
      }
    } catch (SecurityException e) {
      vlog.error("Cannot access the system clipboard: " + e.getMessage());
    }
  }

  public void setSendingEnabled(boolean b) {
    sendButton.setEnabled(b);
  }

  public void actionPerformed(ActionEvent e) {
    Object s = e.getSource();
    if (s instanceof JButton && (JButton)s == clearButton) {
      textArea.setText(new String(""));
    } else if (s instanceof JButton && (JButton)s == sendButton) {
      cc.sendClipboardData(textArea.getText());
      endDialog();
    } else if (s instanceof JButton && (JButton)s == cancelButton) {
      endDialog();
    }
  }

  CConn cc;
  JPanel pt, pb;
  JTextArea textArea;
  JButton clearButton, sendButton, cancelButton;
  static LogWriter vlog = new LogWriter("ClipboardDialog");
}
