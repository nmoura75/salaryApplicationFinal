package com.payroll.service;

import java.math.BigDecimal;

/**
 * Rates and fixed amounts used by the payroll calculation.
 * Values that change every year, such as the minimum wage, belong in yearly data rather than here.
 */
public final class PayrollConstants {

    public static final BigDecimal EMPLOYEE_SOCIAL_SECURITY_RATE = new BigDecimal("0.11");
    public static final BigDecimal EMPLOYER_SOCIAL_SECURITY_RATE = new BigDecimal("0.2375");
    public static final BigDecimal MEAL_ALLOWANCE_EXEMPT_PER_DAY = new BigDecimal("6.00");
    public static final int DEFAULT_WORKING_DAYS = 22;

    private PayrollConstants() {
    }
}
