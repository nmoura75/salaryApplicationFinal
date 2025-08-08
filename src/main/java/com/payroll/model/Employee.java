package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "employees")
@EqualsAndHashCode(exclude = "salaryReceipts")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String taxId;

    @Column(nullable = false, unique = true)
    private String socialSecurityId;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    @Column(nullable = false)
    private BigDecimal socialSecurityContribution;

    @Column(nullable = false)
    private BigDecimal taxIdContribution;

    @Column(nullable = false)
    private BigDecimal totalContributions;

    @Column(nullable = false)
    private BigDecimal netSalary;

    @Column(nullable = false)
    private Integer dependents;

    @Column(nullable = false)
    private Integer familiarSituation;

    private String position;
    private String department;
    private String bankAccount;
    private String email;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private Set<SalaryReceipt> salaryReceipts = new HashSet<>();
} 