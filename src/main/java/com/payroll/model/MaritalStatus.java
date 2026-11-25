package com.payroll.model;

/**
 * Stored in {@code familiar_situation}. Code 1 is not married. Code 2 is married.
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
        for (MaritalStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown marital status code: " + code);
    }
}
