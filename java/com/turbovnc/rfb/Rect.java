package com.turbovnc.rfb;

public class Rect {

  public Rect() {
    tl = new Point(0, 0);
    br = new Point(0, 0);
  }

  public Rect(Point tl_, Point br_) {
    tl = new Point(tl_.x, tl_.y);
    br = new Point(br_.x, br_.y);
  }

  public Rect(int x1, int y1, int x2, int y2) {
    tl = new Point(x1, y1);
    br = new Point(x2, y2);
  }

  public final void setXYWH(int x, int y, int w, int h) {
    tl.x = x;  tl.y = y;  br.x = x + w;  br.y = y + h;
  }

  public final boolean equals(Rect r) {
    return r.tl.equals(tl) && r.br.equals(br);
  }

  public final boolean isEmpty() {
    return (tl.x >= br.x) || (tl.y >= br.y);
  }

  public final void clear() { tl = new Point();  br = new Point(); }

  public final boolean enclosedBy(Rect r) {
    return (tl.x >= r.tl.x) && (tl.y >= r.tl.y) &&
           (br.x <= r.br.x) && (br.y <= r.br.y);
  }

  public final boolean overlaps(Rect r) {
    return tl.x < r.br.x && tl.y < r.br.y && br.x > r.tl.x && br.y > r.tl.y;
  }

  public final Rect intersection(Rect r) {
    if (!overlaps(r))
      return new Rect(0, 0, 0, 0);
    return new Rect(Math.max(tl.x, r.tl.x), Math.max(tl.y, r.tl.y),
                    Math.min(br.x, r.br.x), Math.min(br.y, r.br.y));
  }

  public final int area() {
    int area = (br.x - tl.x) * (br.y - tl.y);
    if (area > 0)
      return area;
    return 0;
  }

  public final Point dimensions() { return new Point(width(), height()); }

  public final int width() { return br.x - tl.x; }

  public final int height() { return br.y - tl.y; }

  public final boolean contains(Point p) {
    return (tl.x <= p.x) && (tl.y <= p.y) && (br.x > p.x) && (br.y > p.y);
  }

  // CHECKSTYLE VisibilityModifier:OFF
  public Point tl;
  public Point br;
  // CHECKSTYLE VisibilityModifier:ON
}
