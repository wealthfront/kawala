package com.kaching.platform.pdf;


public interface PDFAnnotation {
  <T> T accept(PDFAnnotationVisitor<T> visitor);
}
