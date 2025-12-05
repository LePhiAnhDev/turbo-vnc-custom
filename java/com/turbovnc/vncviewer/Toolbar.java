package com.turbovnc.vncviewer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.*;

import com.turbovnc.rdr.*;
import com.turbovnc.rfb.*;

public final class Toolbar extends JToolBar implements ActionListener {

  static final String[] BUTTONS = {
    "Connection options...", "Connection info...",
    "Request screen refresh", "Request lossless refresh",
    "Save remote desktop image",
    "New Connection...", "Disconnect"
  };

  private final ClassLoader cl = getClass().getClassLoader();
  private final ImageIcon toolbarIcons =
    new ImageIcon(cl.getResource("com/turbovnc/vncviewer/toolbar.png"));
  private final Image toolbarImage = toolbarIcons.getImage();

  public Toolbar(CConn cc_) {
    super();
    cc = cc_;
    BufferedImage bi =
      new BufferedImage(300, 20, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = bi.createGraphics();
    g.drawImage(toolbarImage, 0, 0, 300, 20, null);
    setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    setAlignmentY(java.awt.Component.CENTER_ALIGNMENT);
    setFloatable(false);
    setBorder(new EmptyBorder(1, 2, 1, 0));
    // Map old button indices to new ones for icon extraction
    // Old: 0=options, 1=info, 2=fullscreen, 3=refresh, 4=lossless, 5=screenshot, 6=zoomin, 7=zoom100, 8=zoomout, 9=ctrlaltdel, 10=ctrlesc, 11=ctrl, 12=alt, 13=newconn, 14=disconnect
    // New: 0=options, 1=info, 2=refresh, 3=lossless, 4=screenshot, 5=newconn, 6=disconnect
    int[] iconMap = {0, 1, 3, 4, 5, 13, 14};
    for (int i = 0; i < BUTTONS.length; i++) {
      if (i >= 5 && i <= 6 && cc.params.noNewConn.get())
        continue;
      ImageIcon icon = new ImageIcon(
        tk.createImage(bi.getSubimage(iconMap[i] * 20, 0, 20, 20).getSource()));
      AbstractButton button = new JButton(icon);
      button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      button.setName(BUTTONS[i]);
      button.setToolTipText(BUTTONS[i]);
      button.setBorderPainted(false);
      button.setFocusPainted(false);
      button.setFocusable(false);
      button.addActionListener(this);
      button.addMouseListener(new ButtonListener(button));
      button.setContentAreaFilled(false);
      add(button);
      add(Box.createHorizontalStrut(2));
      if (i == 1 || (i == 4 && !cc.params.noNewConn.get())) {
        // ref http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4346610
        add(new JSeparator(JSeparator.VERTICAL) {
          public Dimension getMaximumSize() {
            return new Dimension(getPreferredSize().width,
                                 Integer.MAX_VALUE);
          }
        });
        add(Box.createHorizontalStrut(2));
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object s = e.getSource();
    if (((AbstractButton)s).getName() == BUTTONS[0]) {
      cc.options.showDialog(cc.viewport);
    } else if (((AbstractButton)s).getName() == BUTTONS[1]) {
      cc.showInfo();
    } else if (((AbstractButton)s).getName() == BUTTONS[2]) {
      cc.refresh();
    } else if (((AbstractButton)s).getName() == BUTTONS[3]) {
      cc.losslessRefresh();
    } else if (((AbstractButton)s).getName() == BUTTONS[4]) {
      cc.screenshot();
    } else if (((AbstractButton)s).getName() == BUTTONS[5]) {
      VncViewer.newViewer(cc.viewer);
    } else if (((AbstractButton)s).getName() == BUTTONS[6]) {
      cc.close();
    }
  }

  public class ButtonListener implements MouseListener {
    Border raised = new BevelBorder(BevelBorder.RAISED);
    Border lowered = new BevelBorder(BevelBorder.LOWERED);
    Border inactive = new EmptyBorder(2, 2, 2, 2);
    AbstractButton b;
    public ButtonListener(javax.swing.AbstractButton button) {
      b = button;
    }
    public void mousePressed(MouseEvent e) {
      if (!b.isEnabled()) return;
      if (b instanceof javax.swing.JToggleButton) {
        b.setBorder((b.isSelected() ? inactive : lowered));
        b.setBorderPainted((b.isSelected() ? false : true));
      } else {
        b.setBorder(lowered);
        b.setBorderPainted(true);
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (!b.isEnabled()) return;
      if (b instanceof javax.swing.JButton) {
        b.setBorder(inactive);
        b.setBorderPainted(false);
      }
    }

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {
      if (!b.isEnabled()) return;
      if (b instanceof javax.swing.JToggleButton && b.isSelected())
        return;
      b.setBorder(raised);
      b.setBorderPainted(true);
    }

    public void mouseExited(MouseEvent e) {
      if (!b.isEnabled()) return;
      if (b instanceof javax.swing.JToggleButton && b.isSelected())
        return;
      b.setBorder(inactive);
      b.setBorderPainted(false);
    }
  }

  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    if (Utils.isWindows()) {
      double displayScalingFactor = g2.getTransform().getScaleX();
      Object scalingAlg = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
      if ((displayScalingFactor % 1.0) == 0.0)
        scalingAlg = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      String alg = System.getProperty("turbovnc.scalingalg");
      if (alg != null) {
        if (alg.equalsIgnoreCase("bicubic"))
          scalingAlg = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        else if (alg.equalsIgnoreCase("bilinear"))
          scalingAlg = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        else if (alg.equalsIgnoreCase("nearestneighbor"))
          scalingAlg = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      }
      if (displayScalingFactor != 1.0)
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scalingAlg);
    }
    super.paintComponent(g);
  }

  private CConn cc;
  static Toolkit tk = Toolkit.getDefaultToolkit();
  static LogWriter vlog = new LogWriter("Toolbar");
}
