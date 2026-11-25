package com.payroll.config;

import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@Component
public class Messages {

    private final ResourceBundle bundle;
    private final Locale locale;

    public Messages(@Value("${payroll.locale:pt}") String localeTag) {
        this.locale = Locale.forLanguageTag(localeTag);
        this.bundle = ResourceBundle.getBundle("i18n.messages", locale, new Utf8Control());
    }

    public Locale locale() {
        return locale;
    }

    public ResourceBundle bundle() {
        return bundle;
    }

    public String text(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        }
        return key;
    }

    public String format(String key, Object... arguments) {
        return MessageFormat.format(text(key), arguments);
    }

    public String enumText(Enum<?> value) {
        if (value == null) {
            return "";
        }
        return text("enum." + value.getDeclaringClass().getSimpleName() + "." + value.name());
    }

    public <E extends Enum<E>> StringConverter<E> enumConverter(Class<E> type) {
        return new StringConverter<>() {
            @Override
            public String toString(E value) {
                return enumText(value);
            }

            @Override
            public E fromString(String value) {
                if (value == null || value.isBlank()) {
                    return null;
                }
                for (E constant : type.getEnumConstants()) {
                    if (enumText(constant).equals(value) || constant.name().equals(value)) {
                        return constant;
                    }
                }
                return null;
            }
        };
    }

    /**
     * {@link PropertyResourceBundle} reads {@code .properties} as ISO-8859-1 unless a reader supplies the encoding.
     */
    private static final class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            if (!"java.properties".equals(format)) {
                return super.newBundle(baseName, locale, format, loader, reload);
            }
            String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) {
                    return null;
                }
                return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        }
    }
}
