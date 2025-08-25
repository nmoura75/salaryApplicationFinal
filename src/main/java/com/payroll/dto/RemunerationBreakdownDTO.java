package com.payroll.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemunerationBreakdownDTO {
    
    private Integer categoryId;
    private String categoryName;
    private String categoryType;
    private BigDecimal dailyValue;
    private BigDecimal monthlyValue;
    private BigDecimal totalAmount;
    private Integer businessDays; // Only for daily type
    private String notes;
    
    public RemunerationBreakdownDTO(Integer categoryId, String categoryName, String categoryType, 
                                   BigDecimal totalAmount, Integer businessDays) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.totalAmount = totalAmount;
        this.businessDays = businessDays;
    }
}
