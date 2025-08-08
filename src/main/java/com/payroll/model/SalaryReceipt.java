package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "salary_receipts")
public class SalaryReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    private BigDecimal overtime;
    private BigDecimal bonus;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal otherAllowances;

    @Column(nullable = false)
    private BigDecimal socialSecurityContribution;

    @Column(nullable = false)
    private BigDecimal incomeTax;

    @Column(nullable = false)
    private BigDecimal grossAmount;

    @Column(nullable = false)
    private BigDecimal netAmount;

    private String notes;
} 