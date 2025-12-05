package com.turbovnc.vncviewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.reflect.*;

import com.turbovnc.rfb.*;

public final class MacMenuBar extends JMenuBar implements ActionListener {

  // The following code allows us to pop up our own dialogs whenever
  // "Preferences" is selected from the application menu.

  class MyInvocationHandler implements InvocationHandler {
    MyInvocationHandler(CConn cc_) { cc = cc_; }

    public Object invoke(Object proxy, Method method, Object[] args) {
      if (method.getName().equals("handlePreferences")) {
        if (!cc.options.isShown())
          cc.options.showDialog(cc.viewport);
      } else if (method.getName().equals("handleQuitRequestWith")) {
        try {
          Class quitResponseClass;

          if (Utils.JAVA_VERSION >= 9)
            quitResponseClass = Class.forName("java.awt.desktop.QuitResponse");
          else
            quitResponseClass = Class.forName("com.apple.eawt.QuitResponse");

          Method cancelQuit =
            quitResponseClass.getMethod("cancelQuit", (Class[])null);
          Method performQuit =
            quitResponseClass.getMethod("performQuit", (Class[])null);

          if (cc.confirmClose())
            performQuit.invoke(args[1]);
          else
            cancelQuit.invoke(args[1]);
        } catch (Exception e) {
          vlog.info("Could not handle quit request:");
          vlog.info("  " + e.toString());
        }
      }
      return null;
    }

    CConn cc;
  }

  void setupAppMenu() {
    try {
      Class appClass, prefsHandlerClass, quitHandlerClass;
      Object obj;

      if (Utils.JAVA_VERSION >= 9) {
        appClass = Desktop.class;
        obj = Desktop.getDesktop();
        prefsHandlerClass =
          Class.forName("java.awt.desktop.PreferencesHandler");
        quitHandlerClass = Class.forName("java.awt.desktop.QuitHandler");
      } else {
        appClass = Class.forName("com.apple.eawt.Application");
        Method getApplication = appClass.getMethod("getApplication",
                                                   (Class[])null);
        obj = getApplication.invoke(appClass);
        prefsHandlerClass = Class.forName("com.apple.eawt.PreferencesHandler");
        quitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
      }

      InvocationHandler prefsHandler = new MyInvocationHandler(cc);
      proxy = Proxy.newProxyInstance(prefsHandlerClass.getClassLoader(),
                                     new Class[]{ prefsHandlerClass },
                                     prefsHandler);
      Method setPreferencesHandler =
        appClass.getMethod("setPreferencesHandler", prefsHandlerClass);
      setPreferencesHandler.invoke(obj, new Object[]{ proxy });

      InvocationHandler quitHandler = new MyInvocationHandler(cc);
      proxy = Proxy.newProxyInstance(quitHandlerClass.getClassLoader(),
                                     new Class[]{ quitHandlerClass },
                                     quitHandler);
      Method setQuitHandler =
        appClass.getMethod("setQuitHandler", quitHandlerClass);
      setQuitHandler.invoke(obj, new Object[]{ proxy });
    } catch (Exception e) {
      vlog.info("Could not override Preferences menu item:");
      vlog.info("  " + e.toString());
    }
  }

  public MacMenuBar(CConn cc_) {
    cc = cc_;
    int acceleratorMask = VncViewer.getMenuShortcutKeyMask();

    setupAppMenu();

    JMenu connMenu = new JMenu("Connection");
    if (!cc.params.noNewConn.get()) {
      newConn = addMenuItem(connMenu, "New Connection...");
      if (!cc.params.noMacHotkeys.get())
        newConn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                      acceleratorMask));
      closeConn = addMenuItem(connMenu, "Close Connection");
      if (!cc.params.noMacHotkeys.get())
        closeConn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                        acceleratorMask));
      connMenu.addSeparator();
    }
    info = addMenuItem(connMenu, "Connection Info...");
    if (!cc.params.noMacHotkeys.get())
      info.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
                                                 acceleratorMask));
    profile = new JCheckBoxMenuItem("Performance Info...");
    profile.setSelected(cc.profileDialog.isVisible());
    profile.addActionListener(this);
    connMenu.add(profile);
    if (!cc.params.noMacHotkeys.get())
      profile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                    acceleratorMask));

    connMenu.addSeparator();
    refresh = addMenuItem(connMenu, "Request Screen Refresh");
    if (!cc.params.noMacHotkeys.get())
      refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                    acceleratorMask));
    losslessRefresh = addMenuItem(connMenu, "Request Lossless Refresh");
    if (!cc.params.noMacHotkeys.get())
      losslessRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                                                            acceleratorMask));
    screenshot = addMenuItem(connMenu, "Save Remote Desktop Image");
    if (!cc.params.noMacHotkeys.get())
      screenshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                                                       acceleratorMask));
    tileWindows = addMenuItem(connMenu, "Tile All Viewer Windows");
    if (!cc.params.noMacHotkeys.get())
      tileWindows.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                        acceleratorMask));
    showToolbar = new JCheckBoxMenuItem("Show Toolbar");
    showToolbar.setSelected(cc.params.toolbar.get());
    showToolbar.addActionListener(this);
    connMenu.add(showToolbar);
    if (!cc.params.noMacHotkeys.get())
      showToolbar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                                        acceleratorMask));
    connMenu.addSeparator();
    viewOnly = new JCheckBoxMenuItem("View Only");
    viewOnly.setSelected(cc.params.viewOnly.get());
    viewOnly.addActionListener(this);
    connMenu.add(viewOnly);
    connMenu.addSeparator();
    clipboard = addMenuItem(connMenu, "Clipboard ...");

    add(connMenu);
  }

  JMenuItem addMenuItem(JMenu menu, String str, int mnemonic) {
    JMenuItem item = new JMenuItem(str, mnemonic);
    item.addActionListener(this);
    menu.add(item);
    return item;
  }

  JMenuItem addMenuItem(JMenu menu, String str) {
    JMenuItem item = new JMenuItem(str);
    item.addActionListener(this);
    menu.add(item);
    return item;
  }

  boolean actionMatch(ActionEvent ev, JMenuItem item) {
    return ev.getActionCommand().equals(item.getActionCommand());
  }

  public void actionPerformed(ActionEvent ev) {
    if (actionMatch(ev, tileWindows)) {
      VncViewer.tileWindows();
    } else if (actionMatch(ev, showToolbar)) {
      cc.toggleToolbar();
      showToolbar.setSelected(cc.params.toolbar.get());
    } else if (actionMatch(ev, viewOnly)) {
      cc.toggleViewOnly();
      viewOnly.setSelected(cc.params.viewOnly.get());
    } else if (actionMatch(ev, clipboard)) {
      cc.clipboardDialog.showDialog(cc.viewport);
    } else if (actionMatch(ev, refresh)) {
      cc.refresh();
    } else if (actionMatch(ev, losslessRefresh)) {
      cc.losslessRefresh();
    } else if (actionMatch(ev, screenshot)) {
      cc.screenshot();
    } else if (!cc.params.noNewConn.get() && actionMatch(ev, newConn)) {
      VncViewer.newViewer(cc.viewer);
    } else if (!cc.params.noNewConn.get() && actionMatch(ev, closeConn)) {
      cc.close();
    } else if (actionMatch(ev, info)) {
      cc.showInfo();
    } else if (actionMatch(ev, profile)) {
      if (cc.profileDialog.isVisible())
        cc.profileDialog.endDialog();
      else
        cc.profileDialog.showDialog(cc.viewport);
      cc.toggleProfile();
    }
  }

  void updateProfile() {
    profile.setSelected(cc.profileDialog.isVisible());
  }

  CConn cc;
  JMenuItem tileWindows;
  JMenuItem clipboard, refresh, losslessRefresh;
  JMenuItem newConn, closeConn, info, screenshot;
  JCheckBoxMenuItem profile, showToolbar, viewOnly;
  static LogWriter vlog = new LogWriter("MacMenuBar");
}
