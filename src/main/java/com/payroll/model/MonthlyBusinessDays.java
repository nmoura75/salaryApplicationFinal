package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "monthly_business_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBusinessDays {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "month", nullable = false)
    private Integer month;
    
    @Column(name = "business_days", nullable = false)
    private Integer businessDays;
    
    @Column(name = "last_updated")
    private String lastUpdated;
    
    @Column(name = "notes")
    private String notes;
    
    // Composite unique constraint for company + year + month
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "year", "month"})
    })
    public static class MonthlyBusinessDaysTable {}
}
