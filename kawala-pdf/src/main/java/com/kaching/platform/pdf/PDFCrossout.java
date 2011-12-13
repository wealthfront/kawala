package com.kaching.platform.pdf;

public class PDFCrossout implements PDFAnnotation {
  float width;
  int startX;
  int startY;
  int endX;
  int endY;
  int page;

  public PDFCrossout(float width, int startX, int startY, int endX, int endY, int page) {
    this.width = width;
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
    this.page = page;
  }

  @Override
  public <T> T accept(PDFAnnotationVisitor<T> visitor) {
    return visitor.visitCrossout(this);
  }

}
