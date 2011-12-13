package com.kaching.platform.pdf;

public class PDFFormatters {
  public static class DefaultPDFFormatter implements PDFFormatter {
    @Override
    public boolean canConvert(Class<?> klass) {
      return true;
    }

    @Override
    public String convertToString(Object o) {
      return o.toString();
    }
  }
}
