package com.payroll.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Amounts produced by {@link com.payroll.service.PayrollCalculator}.
 */
@Data
public class PayrollResult {

    private BigDecimal gross;
    private BigDecimal subjectAmount;
    private BigDecimal employeeSocialSecurity;
    private BigDecimal employerSocialSecurity;
    private BigDecimal incomeTax;
    private BigDecimal net;
}
