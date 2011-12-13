package com.kaching.platform.pdf;

public abstract class PDFAnnotationVisitor<T> {
  abstract T visitStickyNote(PDFStickyNote stickyNote);

  abstract T visitCrossout(PDFCrossout pdfCrossout);
}
