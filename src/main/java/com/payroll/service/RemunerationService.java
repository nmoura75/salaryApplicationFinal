package com.payroll.service;

import com.payroll.model.*;
import com.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class RemunerationService {
    
    private final RemunerationCategoryRepository categoryRepository;
    private final EmployeeRemunerationRepository employeeRemunerationRepository;
    private final MonthlyRemunerationRepository monthlyRemunerationRepository;
    private final MonthlyBusinessDaysRepository monthlyBusinessDaysRepository;
    
    /**
     * Calculate total remuneration for an employee for a specific month
     */
    public BigDecimal calculateMonthlyRemuneration(Integer employeeId, Integer companyId, Integer year, Integer month) {
        BigDecimal total = BigDecimal.ZERO;
        
        // Get all active remuneration categories for the company
        List<RemunerationCategory> categories = categoryRepository.findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(companyId);
        
        for (RemunerationCategory category : categories) {
            BigDecimal amount = calculateCategoryAmount(category, employeeId, companyId, year, month);
            total = total.add(amount);
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate amount for a specific remuneration category
     */
    private BigDecimal calculateCategoryAmount(RemunerationCategory category, Integer employeeId, 
                                            Integer companyId, Integer year, Integer month) {
        
        // First check if there's a monthly override
        Optional<MonthlyRemuneration> monthlyRemuneration = monthlyRemunerationRepository
            .findByEmployeeIdAndCategoryIdAndYearAndMonth(employeeId, category.getId(), year, month);
        
        if (monthlyRemuneration.isPresent()) {
            MonthlyRemuneration mr = monthlyRemuneration.get();
            if (mr.isOverride()) {
                // This is an override, use the monthly value
                return mr.getValue() != null ? mr.getValue() : BigDecimal.ZERO;
            } else {
                // This is additional to the default, add it to the default calculation
                BigDecimal defaultAmount = calculateDefaultAmount(category, employeeId, companyId, year, month);
                BigDecimal additionalAmount = mr.getValue() != null ? mr.getValue() : BigDecimal.ZERO;
                return defaultAmount.add(additionalAmount);
            }
        }
        
        // No monthly override, use default calculation
        return calculateDefaultAmount(category, employeeId, companyId, year, month);
    }
    
    /**
     * Calculate default amount for a category (from employee defaults)
     */
    private BigDecimal calculateDefaultAmount(RemunerationCategory category, Integer employeeId, 
                                           Integer companyId, Integer year, Integer month) {
        
        // Get employee's default value for this category
        List<EmployeeRemuneration> employeeRemunerations = employeeRemunerationRepository
            .findByEmployeeIdAndCategoryIdAndIsActiveTrue(employeeId, category.getId());
        
        BigDecimal value = BigDecimal.ZERO;
        if (!employeeRemunerations.isEmpty()) {
            value = employeeRemunerations.get(0).getValue();
        }
        
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        switch (category.getType()) {
            case DAILY:
                return calculateDailyAmount(value, companyId, year, month);
            case MONTHLY:
                return value;
            case PERCENTAGE:
                // For percentage-based, we need base salary - this will be handled in salary calculation
                return value;
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate daily-based remuneration (e.g., meal allowance)
     */
    private BigDecimal calculateDailyAmount(BigDecimal dailyValue, Integer companyId, Integer year, Integer month) {
        Optional<MonthlyBusinessDays> businessDays = monthlyBusinessDaysRepository
            .findByCompanyIdAndYearAndMonth(companyId, year, month);
        
        if (businessDays.isPresent()) {
            return dailyValue.multiply(BigDecimal.valueOf(businessDays.get().getBusinessDays()));
        } else {
            // Fallback: calculate business days automatically
            int calculatedBusinessDays = calculateBusinessDaysForMonth(year, month);
            return dailyValue.multiply(BigDecimal.valueOf(calculatedBusinessDays));
        }
    }
    
    /**
     * Calculate business days for a month (excluding weekends)
     */
    private int calculateBusinessDaysForMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int businessDays = 0;
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() <= 5) { // Monday = 1, Friday = 5
                businessDays++;
            }
        }
        
        return businessDays;
    }
    
    /**
     * Set monthly business days for a company
     */
    public MonthlyBusinessDays setMonthlyBusinessDays(Integer companyId, Integer year, Integer month, Integer businessDays) {
        Optional<MonthlyBusinessDays> existing = monthlyBusinessDaysRepository
            .findByCompanyIdAndYearAndMonth(companyId, year, month);
        
        if (existing.isPresent()) {
            MonthlyBusinessDays mbd = existing.get();
            mbd.setBusinessDays(businessDays);
            mbd.setLastUpdated(LocalDate.now().toString());
            return monthlyBusinessDaysRepository.save(mbd);
        } else {
            MonthlyBusinessDays mbd = new MonthlyBusinessDays();
            mbd.setCompanyId(companyId);
            mbd.setYear(year);
            mbd.setMonth(month);
            mbd.setBusinessDays(businessDays);
            mbd.setLastUpdated(LocalDate.now().toString());
            return monthlyBusinessDaysRepository.save(mbd);
        }
    }
    
    /**
     * Get all remuneration categories for a company
     */
    public List<RemunerationCategory> getCategoriesForCompany(Integer companyId) {
        return categoryRepository.findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(companyId);
    }
    
    /**
     * Get employee default remunerations
     */
    public List<EmployeeRemuneration> getEmployeeRemunerations(Integer employeeId) {
        return employeeRemunerationRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
    }
    
    /**
     * Set employee default remuneration for a category
     */
    public EmployeeRemuneration setEmployeeRemuneration(Integer employeeId, Integer categoryId, BigDecimal value) {
        List<EmployeeRemuneration> existingList = employeeRemunerationRepository
            .findByEmployeeIdAndCategoryIdAndIsActiveTrue(employeeId, categoryId);
        
        if (!existingList.isEmpty()) {
            EmployeeRemuneration er = existingList.get(0);
            er.setValue(value);
            return employeeRemunerationRepository.save(er);
        } else {
            EmployeeRemuneration er = new EmployeeRemuneration();
            er.setEmployeeId(employeeId);
            er.setCategoryId(categoryId);
            er.setValue(value);
            er.setActive(true);
            return employeeRemunerationRepository.save(er);
        }
    }
    
    /**
     * Set monthly remuneration (override or additional)
     */
    public MonthlyRemuneration setMonthlyRemuneration(Integer employeeId, Integer categoryId, 
                                                     Integer year, Integer month, BigDecimal value, boolean isOverride) {
        Optional<MonthlyRemuneration> existing = monthlyRemunerationRepository
            .findByEmployeeIdAndCategoryIdAndYearAndMonth(employeeId, categoryId, year, month);
        
        if (existing.isPresent()) {
            MonthlyRemuneration mr = existing.get();
            mr.setValue(value);
            mr.setOverride(isOverride);
            return monthlyRemunerationRepository.save(mr);
        } else {
            MonthlyRemuneration mr = new MonthlyRemuneration();
            mr.setEmployeeId(employeeId);
            mr.setCategoryId(categoryId);
            mr.setYear(year);
            mr.setMonth(month);
            mr.setValue(value);
            mr.setOverride(isOverride);
            return monthlyRemunerationRepository.save(mr);
        }
    }
    
    /**
     * Get monthly remunerations for a specific month
     * Note: This method now returns an empty list since we removed the complex query.
     * In a real implementation, you might want to implement this differently.
     */
    public List<MonthlyRemuneration> getMonthlyRemunerations(Integer companyId, Integer year, Integer month) {
        // TODO: Since we removed the complex query, return empty list for now
        // In a real implementation, you might want to:
        // 1. Get all employees for the company first
        // 2. Then get monthly remunerations for each employee
        // 3. Or implement a different query approach
        return new ArrayList<>();
    }
}
