package com.vulinh.locale;

import static java.util.Collections.enumeration;
import static java.util.Collections.list;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.ResourceBundle.getBundle;
import static java.util.stream.Collectors.toList;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.UnknownFormatConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

@Slf4j
public class Translator {

  private static final String BASE_NAME = "i18n";
  private static final String MESSAGE_PATH = "i18n/messages";

  private static final Control BUNDLE_CONTROL =
      new MultiResourceBundleControl(List.of(MESSAGE_PATH));

  private Translator() {
    throw new UnsupportedOperationException("Cannot instantiate utility class");
  }

  public static String getMessage(I18nCode itemEnum) {
    return getLocalizedMessage(itemEnum);
  }

  public static String getLocalizedMessage(I18nCode i18nEnum, Object... args) {
    String code = i18nEnum.code();

    try {
      Locale locale = getLocale();

      ResourceBundle resourceBundle = getBundle(BASE_NAME, locale, BUNDLE_CONTROL);

      String localizedText = resourceBundle.getString(code);

      return nonNull(args) && args.length > 0
          ? StringUtils.trim(String.format(localizedText, args))
          : StringUtils.trim(localizedText);

    } catch (UnknownFormatConversionException ex) {
      log.warn("LOCALE WARNING: Localized text format error:", ex);
      return code;
    } catch (MissingResourceException ex) {
      log.warn("LOCALE WARNING: Message with code " + i18nEnum.code() + " not found!", ex);
      return code;
    }
  }

  static class MultiResourceBundleControl extends Control {

    private final List<String> dependentBaseNames;

    public MultiResourceBundleControl(List<String> dependentBaseNames) {
      if (isNull(dependentBaseNames) || dependentBaseNames.isEmpty()) {
        throw new IllegalArgumentException("Empty dependentBaseNames!");
      }

      this.dependentBaseNames = dependentBaseNames;
    }

    @Override
    public ResourceBundle newBundle(
        String baseName, Locale locale, String format, ClassLoader loader, boolean reload) {
      return new MultiResourceBundle(
          dependentBaseNames.stream()
              .filter(StringUtils::isNotBlank)
              .map(e -> getBundle(e, locale))
              .collect(toList()));
    }

    static class MultiResourceBundle extends ResourceBundle {

      private final List<ResourceBundle> delegates;

      public MultiResourceBundle(List<ResourceBundle> delegates) {
        this.delegates = delegates;
      }

      @Override
      protected Object handleGetObject(@NonNull String key) {
        return delegates.stream()
            .filter(e -> nonNull(e) && e.containsKey(key))
            .map(e -> e.getObject(key))
            .findFirst()
            .orElse(null);
      }

      @Override
      public Enumeration<String> getKeys() {
        return enumeration(
            delegates.stream()
                .filter(Objects::nonNull)
                .flatMap(e -> list(e.getKeys()).stream())
                .collect(toList()));
      }
    }
  }
}
