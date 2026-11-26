package com.payroll.service;

/**
 * Length rules for the employee tax number and social-security number.
 * A NIF has 8, 9 or 10 digits. A NISS has 11 digits. Spaces are ignored.
 */
public final class IdentificationNumbers {

    public static final int NIF_MIN_DIGITS = 8;
    public static final int NIF_MAX_DIGITS = 10;
    public static final int NISS_DIGITS = 11;

    private IdentificationNumbers() {
    }

    public static String nif(String raw) {
        String digits = digitsOnly(raw);
        if (digits == null || digits.length() < NIF_MIN_DIGITS || digits.length() > NIF_MAX_DIGITS) {
            return null;
        }
        return digits;
    }

    public static String niss(String raw) {
        String digits = digitsOnly(raw);
        if (digits == null || digits.length() != NISS_DIGITS) {
            return null;
        }
        return digits;
    }

    private static String digitsOnly(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder digits = new StringBuilder();
        for (int index = 0; index < raw.length(); index++) {
            char character = raw.charAt(index);
            if (Character.isWhitespace(character)) {
                continue;
            }
            if (!Character.isDigit(character)) {
                return null;
            }
            digits.append(character);
        }
        return digits.toString();
    }
}
