package com.turbovnc.rfb;

public class Point {

  public Point() { x = 0;  y = 0; }
  public Point(int x_, int y_) { x = x_;  y = y_; }
  public final Point negate() { return new Point(-x, -y); }
  public final boolean equals(Point p) { return (x == p.x && y == p.y); }
  public final Point translate(Point p) { return new Point(x + p.x, y + p.y); }
  public final Point subtract(Point p) { return new Point(x - p.x, y - p.y); }

  @SuppressWarnings("checkstyle:VisibilityModifier")
  public int x, y;
}
