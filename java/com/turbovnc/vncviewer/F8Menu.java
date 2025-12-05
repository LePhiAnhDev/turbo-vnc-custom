package com.turbovnc.vncviewer;

import java.awt.Cursor;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.turbovnc.rfb.*;

public final class F8Menu extends JPopupMenu implements ActionListener {
  public F8Menu(CConn cc_) {
    super("VNC Menu");
    setLightWeightPopupEnabled(false);
    cc = cc_;

    if (!cc.params.noNewConn.get()) {
      exit = addMenuItem("Close Connection", KeyEvent.VK_C);
      addSeparator();
    }
    options = addMenuItem("Options..." +
                          (cc.params.noHotkeys.get() ? "" :
                           "   (Ctrl-Alt-Shift-O)"), KeyEvent.VK_O);
    info = addMenuItem("Connection Info..." +
                       (cc.params.noHotkeys.get() ? "" :
                        "  (Ctrl-Alt-Shift-I)"), KeyEvent.VK_I);
    info.setDisplayedMnemonicIndex(11);
    profile = new JCheckBoxMenuItem("Performance Info..." +
                                    (cc.params.noHotkeys.get() ? "" :
                                     "  (Ctrl-Alt-Shift-P)"));
    profile.setMnemonic(KeyEvent.VK_P);
    profile.setSelected(cc.profileDialog.isVisible());
    profile.addActionListener(this);
    add(profile);
    addSeparator();
    refresh = addMenuItem("Request Screen Refresh" +
                          (cc.params.noHotkeys.get() ? "" :
                           "   (Ctrl-Alt-Shift-R)"), KeyEvent.VK_R);
    refresh.setDisplayedMnemonicIndex(15);
    losslessRefresh = addMenuItem("Request Lossless Refresh" +
                                  (cc.params.noHotkeys.get() ? "" :
                                   "   (Ctrl-Alt-Shift-L)"), KeyEvent.VK_L);
    losslessRefresh.setDisplayedMnemonicIndex(8);
    screenshot = addMenuItem("Save Remote Desktop Image..." +
                             (cc.params.noHotkeys.get() ? "" :
                              "   (Ctrl-Alt-Shift-M)"), KeyEvent.VK_M);
    screenshot.setDisplayedMnemonicIndex(21);
    showToolbar = new JCheckBoxMenuItem("Show Toolbar" +
                                        (cc.params.noHotkeys.get() ? "" :
                                         "   (Ctrl-Alt-Shift-T)"));
    showToolbar.setMnemonic(KeyEvent.VK_T);
    showToolbar.setSelected(cc.params.toolbar.get());
    showToolbar.addActionListener(this);
    add(showToolbar);
    tileWindows = addMenuItem("Tile All Viewer Windows" +
                              (cc.params.noHotkeys.get() ? "" :
                               "   (Ctrl-Alt-Shift-X)"), KeyEvent.VK_X);
    addSeparator();
    viewOnly = new JCheckBoxMenuItem("View Only" +
                                     (cc.params.noHotkeys.get() ? "" :
                                      "   (Ctrl-Alt-Shift-V)"));
    viewOnly.setMnemonic(KeyEvent.VK_V);
    viewOnly.setSelected(cc.params.viewOnly.get());
    viewOnly.addActionListener(this);
    add(viewOnly);
    if (Utils.osGrab() && Helper.isAvailable()) {
      grabKeyboard = new JCheckBoxMenuItem("Grab Keyboard" +
                                           (cc.params.noHotkeys.get() ? "" :
                                            "   (Ctrl-Alt-Shift-G)"));
      grabKeyboard.setMnemonic(KeyEvent.VK_G);
      grabKeyboard.setSelected(VncViewer.isKeyboardGrabbed());
      grabKeyboard.addActionListener(this);
      add(grabKeyboard);
    }
    f8 = addMenuItem("Send " + cc.params.menuKey.getStr());
    KeyStroke ks = KeyStroke.getKeyStroke(cc.params.menuKey.getVKeyCode(), 0);
    f8.setAccelerator(ks);
    addSeparator();
    clipboard = addMenuItem("Clipboard...");
    addSeparator();
    if (!cc.params.noNewConn.get()) {
      newConn = addMenuItem("New Connection..." +
                            (cc.params.noHotkeys.get() ? "" :
                             "   (Ctrl-Alt-Shift-N)"), KeyEvent.VK_N);
      addSeparator();
    }
    about = addMenuItem("About TurboVNC Viewer...", KeyEvent.VK_A);
    addSeparator();
    dismiss = addMenuItem("Dismiss Menu");
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    if (Utils.osGrab())
      addPopupMenuListener(new PopupMenuListener() {
        public void popupMenuCanceled(PopupMenuEvent e) {
          if (cc.isGrabSelected())
            cc.viewport.grabKeyboardHelper(true, true);
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {      }
    });
  }

  JMenuItem addMenuItem(String str, int mnemonic) {
    JMenuItem item = new JMenuItem(str, mnemonic);
    item.addActionListener(this);
    add(item);
    return item;
  }

  JMenuItem addMenuItem(String str) {
    JMenuItem item = new JMenuItem(str);
    item.addActionListener(this);
    add(item);
    return item;
  }

  void updateMenuKey() {
    f8.setText("Send " + cc.params.menuKey.getStr());
    KeyStroke ks = KeyStroke.getKeyStroke(cc.params.menuKey.getVKeyCode(), 0);
    f8.setAccelerator(ks);
  }

  boolean actionMatch(ActionEvent ev, JMenuItem item) {
    if (item == null)
      return false;
    return ev.getActionCommand().equals(item.getActionCommand());
  }

  public void actionPerformed(ActionEvent ev) {
    if (!cc.params.noNewConn.get() && actionMatch(ev, exit)) {
      cc.close();
    } else if (actionMatch(ev, showToolbar)) {
      cc.toggleToolbar();
      showToolbar.setSelected(cc.params.toolbar.get());
    } else if (actionMatch(ev, tileWindows)) {
      VncViewer.tileWindows();
    } else if (actionMatch(ev, clipboard)) {
      cc.clipboardDialog.showDialog(cc.viewport);
    } else if (actionMatch(ev, viewOnly)) {
      cc.toggleViewOnly();
    } else if (actionMatch(ev, grabKeyboard)) {
      if (cc.viewport != null)
        cc.viewport.grabKeyboardHelper(grabKeyboard.isSelected());
    } else if (actionMatch(ev, f8)) {
      cc.writeKeyEvent(cc.params.menuKey.getKeySym(), true);
      cc.writeKeyEvent(cc.params.menuKey.getKeySym(), false);
      firePopupMenuCanceled();
    } else if (actionMatch(ev, refresh)) {
      cc.refresh();
      firePopupMenuCanceled();
    } else if (actionMatch(ev, losslessRefresh)) {
      cc.losslessRefresh();
      firePopupMenuCanceled();
    } else if (actionMatch(ev, screenshot)) {
      cc.screenshot();
    } else if (!cc.params.noNewConn.get() && actionMatch(ev, newConn)) {
      VncViewer.newViewer(cc.viewer);
    } else if (actionMatch(ev, options)) {
      cc.options.showDialog(cc.viewport);
    } else if (actionMatch(ev, info)) {
      cc.showInfo();
    } else if (actionMatch(ev, profile)) {
      if (cc.profileDialog.isVisible())
        cc.profileDialog.endDialog();
      else
        cc.profileDialog.showDialog(cc.viewport);
      cc.toggleProfile();
    } else if (actionMatch(ev, about)) {
      cc.showAbout();
    } else if (actionMatch(ev, dismiss)) {
      firePopupMenuCanceled();
    }
  }

  CConn cc;
  JMenuItem tileWindows;
  JMenuItem exit, clipboard, refresh, losslessRefresh;
  JMenuItem newConn, options, info, profile, screenshot, about, dismiss;
  static JMenuItem f8;
  JCheckBoxMenuItem showToolbar, viewOnly, grabKeyboard;

  static LogWriter vlog = new LogWriter("F8Menu");
}
