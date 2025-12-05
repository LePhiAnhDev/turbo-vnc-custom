package com.turbovnc.rfb;

import java.util.*;

public class Screen {

  public Screen() { id = 0;  flags = 0;  dimensions = new Rect(); }

  // Deep copy
  public Screen(Screen old) {
    this(old.id, old.dimensions.tl.x, old.dimensions.tl.y,
         old.dimensions.width(), old.dimensions.height(), old.flags);
  }

  public Screen(int id_, int x_, int y_, int w_, int h_, int flags_) {
    id = id_;
    dimensions = new Rect(x_, y_, x_ + w_, y_ + h_);
    flags = flags_;
  }

  public final boolean equals(Screen r) {
    if (id != r.id)
      return false;
    return equalsIgnoreID(r);
  }

  public final boolean equalsIgnoreID(Screen r) {
    if (!dimensions.equals(r.dimensions))
      return false;
    if (flags != r.flags)
      return false;
    return true;
  }

  public final void generateID(ScreenSet ref) {
    Random rng = new Random();
    int newId;

    if (ref != null) {
      while (true) {
        newId = rng.nextInt();
        int i;

        for (i = 0; i < ref.numScreens(); i++) {
          if (ref.screens.get(i).id == newId)
            break;
        }
        if (i == ref.numScreens())
          break;
      }

      id = newId;
    } else
      id = rng.nextInt();
  }

  // CHECKSTYLE VisibilityModifier:OFF
  public int id;
  public Rect dimensions;
  public int flags;
  // CHECKSTYLE VisibilityModifier:ON
}
