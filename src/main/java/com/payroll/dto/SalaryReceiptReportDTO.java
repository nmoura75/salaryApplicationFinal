package com.payroll.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SalaryReceiptReportDTO {
    private String companyName;
    private String companyTaxId;
    private String companyAddress;
    private String employeeName;
    private String employeeTaxId;
    private String month;
    private Integer year;
    private BigDecimal baseSalary;
    private BigDecimal overtime;
    private BigDecimal bonus;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal otherAllowances;
    private BigDecimal grossAmount;
    private BigDecimal socialSecurityContribution;
    private BigDecimal incomeTax;
    private BigDecimal netAmount;
} 