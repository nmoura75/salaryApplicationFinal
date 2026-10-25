package com.payroll.dto;

import com.payroll.model.RemunerationCategory;
import lombok.Data;

import java.math.BigDecimal;

/**
 * One calculated remuneration line for an employee and month.
 */
@Data
public class RemunerationLine {

    private Integer categoryId;
    private String categoryName;
    private RemunerationCategory.RemunerationType type;
    private RemunerationCategory.ContributionRule contributionRule;
    private BigDecimal amount;
    private Integer businessDays;
}
