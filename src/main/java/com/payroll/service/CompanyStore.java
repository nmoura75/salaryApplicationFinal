package com.payroll.service;

import com.payroll.model.Company;
import com.payroll.model.RemunerationCategory;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.RemunerationCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Writes the single company row inside the SQLite file that is already open.
 */
@Service
@RequiredArgsConstructor
public class CompanyStore {

    private final CompanyRepository companyRepository;
    private final RemunerationCategoryRepository categoryRepository;

    @Transactional
    public Company insert(Company company) {
        company.setTaxIdPassword(null);
        company.setSocialSecurityIdPassword(null);
        Company saved = companyRepository.save(company);
        seedCategories(saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Company requireSingle() {
        return companyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Company not found in the open database"));
    }

    @Transactional(readOnly = true)
    public Company getById(Integer id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Company getByName(String name) {
        return companyRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public Company getByTaxId(String taxId) {
        return companyRepository.findByTaxId(taxId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with tax ID: " + taxId));
    }

    @Transactional
    public Company update(Integer id, Company companyDetails) {
        Company company = getById(id);
        company.setName(companyDetails.getName());
        company.setTaxId(companyDetails.getTaxId());
        company.setTaxIdPassword(null);
        company.setSocialSecurityId(companyDetails.getSocialSecurityId());
        company.setSocialSecurityIdPassword(null);
        company.setAddress(companyDetails.getAddress());
        company.setPhone(companyDetails.getPhone());
        company.setEmail(companyDetails.getEmail());
        return companyRepository.save(company);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!companyRepository.existsById(id)) {
            throw new EntityNotFoundException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }

    private void seedCategories(Integer companyId) {
        saveCategory("Meal Allowance", "Daily meal allowance for business days",
                RemunerationCategory.RemunerationType.DAILY,
                RemunerationCategory.ContributionRule.MEAL_DAILY,
                new BigDecimal("6.00"), companyId, 1);
        saveCategory("Christmas Bonus", "Annual Christmas bonus (13th month)",
                RemunerationCategory.RemunerationType.MONTHLY,
                RemunerationCategory.ContributionRule.SUBJECT,
                BigDecimal.ZERO, companyId, 2);
        saveCategory("Vacation Subsidy", "Vacation allowance",
                RemunerationCategory.RemunerationType.MONTHLY,
                RemunerationCategory.ContributionRule.SUBJECT,
                BigDecimal.ZERO, companyId, 3);
        saveCategory("Commission", "Sales or performance commission",
                RemunerationCategory.RemunerationType.PERCENTAGE,
                RemunerationCategory.ContributionRule.SUBJECT,
                BigDecimal.ZERO, companyId, 4);
        saveCategory("Transport Allowance", "Monthly transport allowance",
                RemunerationCategory.RemunerationType.MONTHLY,
                RemunerationCategory.ContributionRule.SUBJECT,
                BigDecimal.ZERO, companyId, 5);
    }

    private void saveCategory(String name, String description, RemunerationCategory.RemunerationType type,
                              RemunerationCategory.ContributionRule rule, BigDecimal defaultValue,
                              Integer companyId, int sortOrder) {
        RemunerationCategory category = new RemunerationCategory();
        category.setName(name);
        category.setDescription(description);
        category.setType(type);
        category.setContributionRule(rule);
        category.setDefaultValue(defaultValue);
        category.setActive(true);
        category.setCompanyId(companyId);
        category.setSortOrder(sortOrder);
        categoryRepository.save(category);
    }
}
