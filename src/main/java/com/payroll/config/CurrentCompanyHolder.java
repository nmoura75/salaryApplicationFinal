package com.payroll.config;

/**
 * Remembers which company SQLite file the current call should use.
 * The desktop app opens one company at a time.
 */
public final class CurrentCompanyHolder {

    private static final ThreadLocal<String> PATH = new ThreadLocal<>();

    private CurrentCompanyHolder() {
    }

    public static void set(String databasePath) {
        PATH.set(databasePath);
    }

    public static String get() {
        return PATH.get();
    }

    public static void clear() {
        PATH.remove();
    }
}
