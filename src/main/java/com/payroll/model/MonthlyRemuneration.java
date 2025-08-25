package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_remunerations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRemuneration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;
    
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "month", nullable = false)
    private Integer month;
    
    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;  // Override or additional value for this month
    
    @Column(name = "is_override")
    private boolean isOverride = false; // true if overriding default, false if additional
    
    @Column(name = "notes")
    private String notes;
    
    // Composite unique constraint for employee + category + year + month
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "category_id", "year", "month"})
    })
    public static class MonthlyRemunerationTable {}
}
