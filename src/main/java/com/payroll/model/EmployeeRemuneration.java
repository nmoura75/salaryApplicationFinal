package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_remunerations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRemuneration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;
    
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    
    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;  // Employee's default value for this category
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "notes")
    private String notes;
}
