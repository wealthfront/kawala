package com.kaching.platform.pdf;


import java.io.InputStream;
import java.util.List;

import com.kaching.platform.common.Option;

public interface PDFForm {
  public InputStream getPDFInputStream();
  public Option<String> getPDFEncryptionPassword();
  public Option<String> getPageRange();
  public Option<List<PDFAnnotation>> getAnnotations();
  public String getAuthor();
  public String getTitle();
}
