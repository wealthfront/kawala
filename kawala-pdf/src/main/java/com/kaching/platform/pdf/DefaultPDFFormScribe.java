package com.kaching.platform.pdf;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterators.contains;
import static com.google.common.collect.Iterators.forArray;
import static com.kaching.platform.common.Option.of;
import static com.kaching.platform.common.Range.range;
import static com.kaching.platform.common.logging.Log.getLog;
import static java.lang.String.format;
import static java.util.Arrays.sort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.kaching.platform.common.logging.Log;

public class DefaultPDFFormScribe extends VisitorPDFFormScribe<AcroFields> {
  private final static Log log = getLog(DefaultPDFFormScribe.class);
  
  protected final static byte[] PDF_KEY = "00000000000000000000000000000000".getBytes();

  @Override
  public <T extends PDFForm> ByteArrayOutputStream fromPDFFormInstance(Class<T> spec, T instance) {
    try {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      PdfReader reader = new PdfReader(instance.getPDFInputStream(), instance.getPDFEncryptionPassword().getOrElse("").getBytes());
      reader.removeUsageRights();
      for (String pageRange : instance.getPageRange()) {
        reader.selectPages(pageRange);
      }
      final PdfStamper stamper = new PdfStamper(reader, result);
      stamper.setEncryption(null, PDF_KEY, PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
      stamper.setFullCompression();
      stamper.setFormFlattening(true);

      HashMap<String, String> info = reader.getInfo();
      info.put("Producer", "Wealthfront Inc.");
      info.put("Creator", "Wealthfront Inc.");
      info.put("Author", "Wealthfront Inc.");

      stamper.setMoreInfo(info);

      for (List<PDFAnnotation> listOfAnnotations : instance.getAnnotations()) {
        for (PDFAnnotation annotation : listOfAnnotations) {
          // TODO(ian): Replace Void with com.kaching.platform.functional.Unit.
          annotation.accept(new PDFAnnotationVisitor<Void>() {
            @Override
            Void visitStickyNote(PDFStickyNote sticky) {
              PdfContentByte canvas = of(stamper.getOverContent(sticky.page)).getOrThrow("Page " + sticky.page + " doesn't exist");
              Image logoImage;
              try {
                logoImage = Image.getInstance(sticky.source.toURL());
                logoImage.setAbsolutePosition(sticky.x, sticky.y);
                logoImage.setRotationDegrees(sticky.rotation);
                logoImage.scaleToFit(sticky.scaleX, sticky.scaleY);
                canvas.addImage(logoImage);
              } catch (BadElementException e) {
                throw new RuntimeException(e);
              } catch (MalformedURLException e) {
                throw new RuntimeException(e);
              } catch (IOException e) {
                throw new RuntimeException(e);
              } catch (DocumentException e) {
                throw new RuntimeException(e);
              }

              return null;
            }

            @Override
            Void visitCrossout(PDFCrossout xout) {
              PdfContentByte canvas = of(stamper.getOverContent(xout.page)).getOrThrow("Page " + xout.page + " doesn't exist");

              canvas.setLineWidth(xout.width);
              canvas.moveTo(xout.startX, xout.startY);
              canvas.lineTo(xout.endX, xout.endY);
              canvas.stroke();
              canvas.saveState();

              return null;
            }
          });
        }
      }

      visitPDFFields(spec, instance, stamper.getAcroFields());
      stamper.close();
      return result;
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visitSimple(AcroFields form, String field, String value) {
    try {
      checkNotNull(form.getFieldItem(field), "%s with value '%s' is not a known field", field, value);
      form.setField(field, value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visitRange(AcroFields form, String field, Integer from, Integer to, String value) {
    int requiredLength = to - from + 1;
    if (value.length() > requiredLength) {
      throw new RuntimeException("for field " + field + " you need values of length <= " + requiredLength);
    }
    for (Integer i : range(from, value.length())) {
      String fieldName = format("%s%d", field, i);
      try {
        form.setField(fieldName, Character.toString(value.charAt(i - 1)));
      } catch (NullPointerException e) {
        throw new RuntimeException("field " + fieldName + " not found in PDF");
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (DocumentException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void visitCheckbox(AcroFields form, String fieldName, Boolean value) {
    String[] states = form.getAppearanceStates(fieldName);
    checkState(1 == checkNotNull(states, "%s doesn't look like a checkbox", fieldName).length, "%s doesn't look like a checkbox", fieldName);
    try {
      form.setField(fieldName, (value ? states[0] : ""));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void selectOption(AcroFields form, String fieldName, String suboptionName) {
    try {
      form.setField(fieldName, suboptionName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <E extends Enum<E>> boolean visitOption(AcroFields form, String fieldName, Enum<E> value) {
    @SuppressWarnings("rawtypes") Class<? extends Enum> valueType = value.getClass();
    try {
      for (PDFSuboption annotation : of(valueType.getField(value.toString()).getAnnotation(PDFSuboption.class))) {
        String suboptionFieldName = annotation.value();

        if (!contains(forArray(checkNotNull(form.getAppearanceStates(fieldName), "%s doesn't look like a suboption", fieldName)), suboptionFieldName)) {
          log.warn("%s is not a valid suboption for %s", suboptionFieldName, fieldName);
          return false;
        }

        form.setField(fieldName, suboptionFieldName);
        return true;
      }

      for (PDFIndexedSuboption annotation : of(valueType.getField(value.toString()).getAnnotation(PDFIndexedSuboption.class))) {
        String[] appearanceStates = form.getAppearanceStates(fieldName);

        if (annotation.index() >= appearanceStates.length) {
          log.warn("index %d is not a valid suboption for %s with only %d options", annotation.index(), fieldName, form.getAppearanceStates(fieldName).length);
          return false;
        }

        sort(appearanceStates);
        form.setField(fieldName, appearanceStates[annotation.index()]);
        return true;
      }

      return false;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }
}
