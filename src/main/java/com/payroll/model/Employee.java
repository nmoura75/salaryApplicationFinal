package com.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "locality")
    private String locality;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType = ContractType.PERMANENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_time", nullable = false)
    private WorkingTime workingTime = WorkingTime.FULL_TIME;

    @Column(name = "position")
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name = "hire_date", nullable = false)
    private String hireDate;

    @Column(name = "contract_end_date")
    private String contractEndDate;

    @Column(name = "termination_date")
    private String terminationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason")
    private TerminationReason terminationReason;

    @Convert(converter = MaritalStatusConverter.class)
    @Column(name = "familiar_situation", nullable = false)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_holders", nullable = false)
    private IncomeHolders incomeHolders = IncomeHolders.SINGLE_HOLDER;

    @Column(name = "dependents")
    private Integer dependents;

    @Column(name = "disability", nullable = false)
    private boolean disability;

    @Convert(converter = WithholdingTableConverter.class)
    @Column(name = "irs_table")
    private WithholdingTable withholdingTable;

    @Column(name = "base_salary", nullable = false, precision = 38, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "useful_days_of_work", nullable = false)
    private Integer usefulDaysOfWork;

    @Embedded
    private BankDetails bankDetails = new BankDetails();

    @Embedded
    private InsurancePolicy insurancePolicy = new InsurancePolicy();

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
}
