package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @Column(name = "social_security_id", nullable = false, unique = true)
    private String socialSecurityId;

    @Column(name = "hire_date", nullable = false)
    private String hireDate;

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

    @Column(name = "net_salary", nullable = false, precision = 38, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "dependents")
    private Integer dependents;

    @Column(name = "familiar_situation", nullable = false)
    private Integer familiarSituation;

    @Column(name = "useful_days_of_work", nullable = false)
    private Integer usefulDaysOfWork;

    @Column(name = "position")
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "irs_table")
    private Integer irsTable;

    @Column(name = "status")
    private String status;
} 