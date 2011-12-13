package com.kaching.platform.pdf;

import static com.kaching.platform.common.Option.of;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.kaching.platform.common.Errors;


public abstract class VisitorPDFFormScribe<X> implements PDFFormScribe {
  protected Map<Class<? extends PDFFormatter>, PDFFormatter> formatterInstances = new MapMaker()
      .makeComputingMap(new Function<Class<? extends PDFFormatter>, PDFFormatter>() {
        @Override
        public PDFFormatter apply(Class<? extends PDFFormatter> input) {
          try {
            return input.newInstance();
          } catch (InstantiationException e) {
            throw new RuntimeException(e);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }
      });

  @SuppressWarnings("unchecked")
  public <T extends PDFForm> void visitPDFFields(Class<T> spec, T instance, X form) {
    Errors errors = new Errors();

    try {
      for (Field field : spec.getFields()) {
        try {
          for (Object value : of(field.get(instance))) {

            for (PDFCommonOptionOrOtherAndTextfield annotation : of(field.getAnnotation(PDFCommonOptionOrOtherAndTextfield.class))) {
              //            this.visitOption(form, annotation.name(), annotation.subtype().cast(value));
              if (value.toString().equals(annotation.commonOptionValue())) {
                this.selectOption(form, annotation.optionFieldName(), annotation.commonSuboption());
              } else {
                this.selectOption(form, annotation.optionFieldName(), annotation.otherSuboption());
                this.visitSimple(form, annotation.textFieldName(), value.toString());
              }
            }

            for (PDFAlternativeOptions annotation : of(field.getAnnotation(PDFAlternativeOptions.class))) {
              for (PDFOption option : annotation.value()) {
                if (this.visitOption(form, option.name(), option.subtype().cast(value))) {
                  break;
                }
              }
            }

            for (PDFTextfieldInRange annotation : of(field.getAnnotation(PDFTextfieldInRange.class))) {
              this.visitRange(form, annotation.name(), annotation.from(), annotation.to(), value.toString());
            }

            for (PDFTextfield annotation : of(field.getAnnotation(PDFTextfield.class))) {
              this.visitSimple(form, annotation.name(), value.toString());
            }

            for (PDFCheckbox annotation : of(field.getAnnotation(PDFCheckbox.class))) {
              this.visitCheckbox(form, annotation.value(), (Boolean) value);
            }

            for (PDFOption annotation : of(field.getAnnotation(PDFOption.class))) {
              this.visitOption(form, annotation.name(), annotation.subtype().cast(value));
            }
          }
        } catch (RuntimeException e) {
          errors.addMessage("%s - %s", field.getName(), e.toString());
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    errors.throwIfHasErrors();
  }

  /**
   * Handles {@link PDFCheckbox}
   */
  public abstract void visitCheckbox(X form, String name, Boolean value);

  /**
   * Handles {@link PDFTextfield}
   */
  public abstract void visitSimple(X form, String field, String value);

  /**
   * Handles {@link PDFTextfieldInRange}
   */
  public abstract void visitRange(X form, String field, Integer from, Integer until, String value);

  /**
   * Handles {@link PDFOption} with {@link PDFSuboption}, or {@link PDFIndexedSuboption}.
   */
  public abstract <E extends Enum<E>> boolean visitOption(X form, String field, Enum<E> value);

  public abstract void selectOption(X form, String fieldName, String suboptionName);
}
