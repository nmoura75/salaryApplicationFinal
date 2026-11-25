package com.payroll.service;

import com.payroll.dto.RemunerationLine;
import com.payroll.i18n.MessageKeys;
import com.payroll.model.*;
import com.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RemunerationService {

    private final RemunerationCategoryRepository categoryRepository;
    private final EmployeeRemunerationRepository employeeRemunerationRepository;
    private final MonthlyRemunerationRepository monthlyRemunerationRepository;
    private final MonthlyBusinessDaysRepository monthlyBusinessDaysRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Sum of remuneration lines for an employee and month, excluding base salary.
     */
    public BigDecimal calculateMonthlyRemuneration(Integer employeeId, Integer companyId, Integer year, Integer month) {
        return calculateLines(employeeId, companyId, year, month).stream()
                .map(line -> line.getAmount() == null ? BigDecimal.ZERO : line.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculated lines for one employee. Percentage categories are applied to the base salary.
     */
    public List<RemunerationLine> calculateLines(Integer employeeId, Integer companyId, Integer year, Integer month) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        BigDecimal baseSalary = employee.getBaseSalary() == null ? BigDecimal.ZERO : employee.getBaseSalary();
        List<RemunerationLine> lines = new ArrayList<>();
        List<RemunerationCategory> categories = categoryRepository
                .findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(companyId);
        for (RemunerationCategory category : categories) {
            lines.add(buildLine(category, employeeId, companyId, year, month, baseSalary));
        }
        return lines;
    }

    /**
     * Business days stored for the month, or the weekday count when none are stored.
     */
    public int getEffectiveBusinessDays(Integer companyId, Integer year, Integer month) {
        return monthlyBusinessDaysRepository.findByCompanyIdAndYearAndMonth(companyId, year, month)
                .map(MonthlyBusinessDays::getBusinessDays)
                .orElseGet(() -> calculateBusinessDaysForMonth(year, month));
    }

    public MonthlyBusinessDays setMonthlyBusinessDays(Integer companyId, Integer year, Integer month, Integer businessDays) {
        Optional<MonthlyBusinessDays> existing = monthlyBusinessDaysRepository
                .findByCompanyIdAndYearAndMonth(companyId, year, month);

        if (existing.isPresent()) {
            MonthlyBusinessDays monthlyBusinessDays = existing.get();
            monthlyBusinessDays.setBusinessDays(businessDays);
            monthlyBusinessDays.setLastUpdated(LocalDate.now().toString());
            return monthlyBusinessDaysRepository.save(monthlyBusinessDays);
        }

        MonthlyBusinessDays monthlyBusinessDays = new MonthlyBusinessDays();
        monthlyBusinessDays.setCompanyId(companyId);
        monthlyBusinessDays.setYear(year);
        monthlyBusinessDays.setMonth(month);
        monthlyBusinessDays.setBusinessDays(businessDays);
        monthlyBusinessDays.setLastUpdated(LocalDate.now().toString());
        return monthlyBusinessDaysRepository.save(monthlyBusinessDays);
    }

    public List<RemunerationCategory> getCategoriesForCompany(Integer companyId) {
        return categoryRepository.findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(companyId);
    }

    @Transactional
    public void ensureStandardTypes(Integer companyId) {
        for (StandardRemunerations.Type type : StandardRemunerations.types()) {
            if (!categoryRepository.existsByNameAndCompanyId(type.name(), companyId)) {
                categoryRepository.save(StandardRemunerations.newCategory(type, companyId));
            }
        }
    }

    @Transactional
    public RemunerationCategory addCategory(Integer companyId, String socialSecurityCode, String taxAuthorityCode,
                                            String name, RemunerationCategory.RemunerationType type,
                                            BigDecimal defaultValue) {
        String description = requireName(name);
        if (categoryRepository.existsByNameAndCompanyId(description, companyId)) {
            throw new IllegalArgumentException(MessageKeys.INCOME_TYPE_EXISTS);
        }
        RemunerationCategory category = new RemunerationCategory();
        category.setCompanyId(companyId);
        category.setName(description);
        category.setDescription(description);
        category.setSocialSecurityCode(blankToNull(socialSecurityCode));
        category.setTaxAuthorityCode(blankToNull(taxAuthorityCode));
        category.setType(type == null ? RemunerationCategory.RemunerationType.MONTHLY : type);
        category.setContributionRule(RemunerationCategory.ContributionRule.SUBJECT);
        category.setDefaultValue(defaultValue == null ? BigDecimal.ZERO : defaultValue);
        category.setActive(true);
        category.setSortOrder(nextSortOrder(companyId));
        return categoryRepository.save(category);
    }

    @Transactional
    public RemunerationCategory updateCategory(Integer categoryId, String socialSecurityCode, String taxAuthorityCode,
                                               String name, RemunerationCategory.RemunerationType type,
                                               BigDecimal defaultValue) {
        RemunerationCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(MessageKeys.INCOME_TYPE_REQUIRED));
        String description = requireName(name);
        Optional<RemunerationCategory> sameName = categoryRepository.findByNameAndCompanyId(description, category.getCompanyId());
        if (sameName.isPresent() && !sameName.get().getId().equals(categoryId)) {
            throw new IllegalArgumentException(MessageKeys.INCOME_TYPE_EXISTS);
        }
        category.setName(description);
        category.setDescription(description);
        category.setSocialSecurityCode(blankToNull(socialSecurityCode));
        category.setTaxAuthorityCode(blankToNull(taxAuthorityCode));
        category.setType(type == null ? RemunerationCategory.RemunerationType.MONTHLY : type);
        category.setDefaultValue(defaultValue == null ? BigDecimal.ZERO : defaultValue);
        return categoryRepository.save(category);
    }

    @Transactional
    public void replaceMonthlyIncomes(Integer employeeId, List<MonthlyIncomeLine> lines) {
        List<EmployeeRemuneration> existing = employeeRemunerationRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
        Set<Integer> kept = new HashSet<>();
        for (MonthlyIncomeLine line : lines) {
            if (line.categoryId() == null || line.amount() == null) {
                continue;
            }
            RemunerationCategory category = categoryRepository.findById(line.categoryId()).orElse(null);
            if (category == null || StandardRemunerations.isBaseSalary(category)) {
                continue;
            }
            setEmployeeRemuneration(employeeId, line.categoryId(), line.amount(), null);
            kept.add(line.categoryId());
        }
        for (EmployeeRemuneration row : existing) {
            if (!kept.contains(row.getCategoryId())) {
                employeeRemunerationRepository.delete(row);
            }
        }
    }

    public List<EmployeeRemuneration> getEmployeeRemunerations(Integer employeeId) {
        return employeeRemunerationRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
    }

    public EmployeeRemuneration setEmployeeRemuneration(Integer employeeId, Integer categoryId,
                                                        BigDecimal value, String notes) {
        List<EmployeeRemuneration> existingList = employeeRemunerationRepository
                .findByEmployeeIdAndCategoryIdAndIsActiveTrue(employeeId, categoryId);

        if (!existingList.isEmpty()) {
            EmployeeRemuneration employeeRemuneration = existingList.get(0);
            employeeRemuneration.setValue(value);
            employeeRemuneration.setNotes(notes);
            return employeeRemunerationRepository.save(employeeRemuneration);
        }

        EmployeeRemuneration employeeRemuneration = new EmployeeRemuneration();
        employeeRemuneration.setEmployeeId(employeeId);
        employeeRemuneration.setCategoryId(categoryId);
        employeeRemuneration.setValue(value);
        employeeRemuneration.setNotes(notes);
        employeeRemuneration.setActive(true);
        return employeeRemunerationRepository.save(employeeRemuneration);
    }

    public MonthlyRemuneration setMonthlyRemuneration(Integer employeeId, Integer categoryId,
                                                     Integer year, Integer month, BigDecimal value, boolean isOverride) {
        Optional<MonthlyRemuneration> existing = monthlyRemunerationRepository
                .findByEmployeeIdAndCategoryIdAndYearAndMonth(employeeId, categoryId, year, month);

        if (existing.isPresent()) {
            MonthlyRemuneration monthlyRemuneration = existing.get();
            monthlyRemuneration.setValue(value);
            monthlyRemuneration.setOverride(isOverride);
            return monthlyRemunerationRepository.save(monthlyRemuneration);
        }

        MonthlyRemuneration monthlyRemuneration = new MonthlyRemuneration();
        monthlyRemuneration.setEmployeeId(employeeId);
        monthlyRemuneration.setCategoryId(categoryId);
        monthlyRemuneration.setYear(year);
        monthlyRemuneration.setMonth(month);
        monthlyRemuneration.setValue(value);
        monthlyRemuneration.setOverride(isOverride);
        return monthlyRemunerationRepository.save(monthlyRemuneration);
    }

    /**
     * Monthly remuneration rows for every employee of the open company.
     */
    public List<MonthlyRemuneration> getMonthlyRemunerations(Integer companyId, Integer year, Integer month) {
        List<MonthlyRemuneration> rows = new ArrayList<>();
        for (Employee employee : employeeRepository.findByCompanyId(companyId)) {
            rows.addAll(monthlyRemunerationRepository.findByEmployeeIdAndYearAndMonth(employee.getId(), year, month));
        }
        return rows;
    }

    private RemunerationLine buildLine(RemunerationCategory category, Integer employeeId, Integer companyId,
                                       Integer year, Integer month, BigDecimal baseSalary) {
        BigDecimal configuredValue = defaultValue(employeeId, category.getId());
        boolean override = false;
        Optional<MonthlyRemuneration> monthlyRemuneration = monthlyRemunerationRepository
                .findByEmployeeIdAndCategoryIdAndYearAndMonth(employeeId, category.getId(), year, month);
        if (monthlyRemuneration.isPresent() && monthlyRemuneration.get().isOverride()) {
            configuredValue = monthlyRemuneration.get().getValue() == null
                    ? BigDecimal.ZERO : monthlyRemuneration.get().getValue();
            override = true;
        }

        int businessDays = getEffectiveBusinessDays(companyId, year, month);
        BigDecimal amount = amountFor(category, configuredValue, baseSalary, businessDays, override);
        if (monthlyRemuneration.isPresent() && !monthlyRemuneration.get().isOverride()) {
            BigDecimal additional = monthlyRemuneration.get().getValue() == null
                    ? BigDecimal.ZERO : monthlyRemuneration.get().getValue();
            amount = amount.add(additional);
        }

        RemunerationLine line = new RemunerationLine();
        line.setCategoryId(category.getId());
        line.setCategoryName(category.getName());
        line.setType(category.getType());
        line.setContributionRule(category.getContributionRule() == null
                ? RemunerationCategory.ContributionRule.SUBJECT : category.getContributionRule());
        line.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        line.setBusinessDays(category.getType() == RemunerationCategory.RemunerationType.DAILY
                || category.getContributionRule() == RemunerationCategory.ContributionRule.MEAL_DAILY
                ? businessDays : null);
        return line;
    }

    private BigDecimal defaultValue(Integer employeeId, Integer categoryId) {
        List<EmployeeRemuneration> employeeRemunerations = employeeRemunerationRepository
                .findByEmployeeIdAndCategoryIdAndIsActiveTrue(employeeId, categoryId);
        if (employeeRemunerations.isEmpty() || employeeRemunerations.get(0).getValue() == null) {
            return BigDecimal.ZERO;
        }
        return employeeRemunerations.get(0).getValue();
    }

    private BigDecimal amountFor(RemunerationCategory category, BigDecimal value, BigDecimal baseSalary,
                                 int businessDays, boolean override) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (override || category.getType() == RemunerationCategory.RemunerationType.MONTHLY) {
            return value;
        }
        if (category.getType() == RemunerationCategory.RemunerationType.DAILY) {
            return value.multiply(BigDecimal.valueOf(businessDays));
        }
        if (category.getType() == RemunerationCategory.RemunerationType.PERCENTAGE) {
            return baseSalary.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private int calculateBusinessDaysForMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int businessDays = 0;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() <= 5) {
                businessDays++;
            }
        }
        return businessDays;
    }

    private int nextSortOrder(Integer companyId) {
        return categoryRepository.findByCompanyIdOrderBySortOrderAsc(companyId).stream()
                .map(RemunerationCategory::getSortOrder)
                .filter(order -> order != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(MessageKeys.INCOME_TYPE_REQUIRED);
        }
        return name.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
