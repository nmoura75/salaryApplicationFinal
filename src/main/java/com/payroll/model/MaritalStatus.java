package com.payroll.model;

/**
 * Stored in {@code familiar_situation}. Code 2 is married. Codes 1, 3 and 4 are the old
 * single, divorced and widowed values, all read as not married.
 */
public enum MaritalStatus {
    NOT_MARRIED(1),
    MARRIED(2);

    private final int code;

    MaritalStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static MaritalStatus fromCode(int code) {
        if (code == MARRIED.code) {
            return MARRIED;
        }
        if (code == 1 || code == 3 || code == 4) {
            return NOT_MARRIED;
        }
        throw new IllegalArgumentException("Unknown marital status code: " + code);
    }
}
