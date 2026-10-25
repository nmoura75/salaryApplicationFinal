package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "salary_receipts")
@Data
public class SalaryReceipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;
    
    @Column(name = "payment_date", nullable = false)
    private String paymentDate;
    
    @Column(name = "month", nullable = false)
    private String month;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "base_salary", nullable = false, precision = 38, scale = 2)
    private BigDecimal baseSalary;
    
    @Column(name = "social_security_contribution", nullable = false, precision = 38, scale = 2)
    private BigDecimal socialSecurityContribution;

    @Column(name = "employer_social_security_contribution", nullable = false, precision = 38, scale = 2)
    private BigDecimal employerSocialSecurityContribution;
    
    @Column(name = "tax_id_contribution", nullable = false, precision = 38, scale = 2)
    private BigDecimal taxIdContribution;
    
    @Column(name = "total_contributions", nullable = false, precision = 38, scale = 2)
    private BigDecimal totalContributions;
    
    @Column(name = "overtime", precision = 38, scale = 2)
    private BigDecimal overtime;
    
    @Column(name = "bonus", precision = 38, scale = 2)
    private BigDecimal bonus;
    
    @Column(name = "meal_allowance", precision = 38, scale = 2)
    private BigDecimal mealAllowance;
    
    @Column(name = "transport_allowance", precision = 38, scale = 2)
    private BigDecimal transportAllowance;
    
    @Column(name = "other_allowances", precision = 38, scale = 2)
    private BigDecimal otherAllowances;
    
    @Column(name = "income_tax", nullable = false, precision = 38, scale = 2)
    private BigDecimal incomeTax;
    
    @Column(name = "gross_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal grossAmount;
    
    @Column(name = "net_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal netAmount;
    
    @Column(name = "remuneration_breakdown")
    private String remunerationBreakdown;
    
    @Column(name = "total_remuneration", precision = 38, scale = 2)
    private BigDecimal totalRemuneration;
    
    @Column(name = "notes")
    private String notes;
} 