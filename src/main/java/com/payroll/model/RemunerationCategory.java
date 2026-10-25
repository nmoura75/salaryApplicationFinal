package com.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "remuneration_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemunerationCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RemunerationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "contribution_rule", nullable = false)
    private ContributionRule contributionRule = ContributionRule.SUBJECT;
    
    @Column(name = "default_value", precision = 10, scale = 2)
    private BigDecimal defaultValue;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    public enum RemunerationType {
        DAILY,      // e.g., Meal allowance (per business day)
        MONTHLY,    // e.g., Christmas bonus, vacation subsidy
        PERCENTAGE  // e.g., Commission based on salary or sales
    }

    public enum ContributionRule {
        SUBJECT,
        EXEMPT,
        MEAL_DAILY
    }
}
