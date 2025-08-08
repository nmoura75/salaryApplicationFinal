package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "companies")
@EqualsAndHashCode(exclude = "employees")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String taxId;

    @Column(nullable = false)
    private String taxIdPassword;

    @Column(nullable = false, unique = true)
    private String socialSecurityId;

    @Column(nullable = false)
    private String socialSecurityIdPassword;

    private String address;
    private String phone;
    private String email;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<Employee> employees = new HashSet<>();
} 