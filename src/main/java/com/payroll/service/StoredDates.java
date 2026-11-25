package com.payroll.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Employee dates are stored as text. New values use ISO-8601. Older rows may use other patterns.
 */
public final class StoredDates {

    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<DateTimeFormatter> STORED_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private StoredDates() {
    }

    public static String iso(LocalDate date) {
        return date == null ? null : date.toString();
    }

    public static LocalDate parse(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String value = stored.trim();
        for (DateTimeFormatter format : STORED_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException notThisFormat) {
                // Fall through to the next candidate format.
            }
        }
        return null;
    }

    public static String display(String stored) {
        LocalDate date = parse(stored);
        return date == null ? stored : date.format(DISPLAY);
    }
}
