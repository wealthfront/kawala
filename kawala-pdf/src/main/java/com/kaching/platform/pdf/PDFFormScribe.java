package com.kaching.platform.pdf;

import java.io.ByteArrayOutputStream;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultPDFFormScribe.class)
public interface PDFFormScribe {
  <T extends PDFForm> ByteArrayOutputStream fromPDFFormInstance(Class<T> spec, T instance);
}