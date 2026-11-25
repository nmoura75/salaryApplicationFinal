package com.payroll.reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * ISO country codes and names loaded from {@code reference/countries.csv}.
 */
public final class CountryDirectory {

    private static final List<Country> COUNTRIES = load();

    private CountryDirectory() {
    }

    public static List<Country> sorted(Locale locale) {
        Locale order = locale == null ? Locale.ENGLISH : locale;
        Collator collator = Collator.getInstance(order);
        return COUNTRIES.stream()
                .sorted(Comparator.comparing(country -> country.name(order), collator))
                .toList();
    }

    public static Optional<Country> find(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String wanted = code.trim();
        return COUNTRIES.stream()
                .filter(country -> country.code().equalsIgnoreCase(wanted))
                .findFirst();
    }

    public static List<Country> countries() {
        return COUNTRIES;
    }

    private static List<Country> load() {
        InputStream stream = CountryDirectory.class.getResourceAsStream("/reference/countries.csv");
        if (stream == null) {
            throw new IllegalStateException("Country list is missing");
        }
        List<Country> countries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.replace("\uFEFF", "").trim();
                if (value.isEmpty() || value.startsWith("code;")) {
                    continue;
                }
                String[] parts = value.split(";", 3);
                if (parts.length < 3) {
                    continue;
                }
                countries.add(new Country(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the country list", exception);
        }
        return List.copyOf(countries);
    }

    public record Country(String code, String namePt, String nameEn) {
        public String name(Locale locale) {
            if (locale != null && "pt".equals(locale.getLanguage())) {
                return namePt;
            }
            return nameEn;
        }
    }
}
