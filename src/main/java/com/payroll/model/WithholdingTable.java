package com.payroll.model;

/**
 * Continente employment withholding tables, stored as the existing {@code irs_table} codes 1 to 7.
 */
public enum WithholdingTable {
    TABLE_I(1),
    TABLE_II(2),
    TABLE_III(3),
    TABLE_IV(4),
    TABLE_V(5),
    TABLE_VI(6),
    TABLE_VII(7);

    private final int code;

    WithholdingTable(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static WithholdingTable fromCode(int code) {
        for (WithholdingTable table : values()) {
            if (table.code == code) {
                return table;
            }
        }
        throw new IllegalArgumentException("Unknown withholding table code: " + code);
    }
}
