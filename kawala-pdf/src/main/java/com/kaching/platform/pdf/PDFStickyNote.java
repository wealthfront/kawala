package com.kaching.platform.pdf;

import java.net.URI;

public class PDFStickyNote implements PDFAnnotation {
  URI source;

  int x, y;

  float rotation;

  float scaleX, scaleY;

  int page;

  public PDFStickyNote(URI source, int x, int y, float rotation, float scaleX, float scaleY, int page) {
    this.source = source;
    this.x = x;
    this.y = y;
    this.rotation = rotation;
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.page = page;
  }

  @Override
  public <T> T accept(PDFAnnotationVisitor<T> visitor) {
    return visitor.visitStickyNote(this);
  }
}
