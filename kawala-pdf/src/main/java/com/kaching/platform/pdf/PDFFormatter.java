package com.kaching.platform.pdf;

public interface PDFFormatter {
  public boolean canConvert(Class<?> klass);
  public String convertToString(Object o);
}
