package com.payroll.service;

import com.payroll.model.Company;
import com.payroll.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Company createCompany(Company company) {
        validateNewCompany(company);
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompany(Integer id, Company companyDetails) {
        Company company = getCompanyById(id);
        
        if (!company.getName().equals(companyDetails.getName()) && 
            companyRepository.existsByName(companyDetails.getName())) {
            throw new IllegalArgumentException("Company name already exists");
        }
        
        if (!company.getTaxId().equals(companyDetails.getTaxId()) && 
            companyRepository.existsByTaxId(companyDetails.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }
        
        if (!company.getSocialSecurityId().equals(companyDetails.getSocialSecurityId()) && 
            companyRepository.existsBySocialSecurityId(companyDetails.getSocialSecurityId())) {
            throw new IllegalArgumentException("Social Security ID already exists");
        }

        //TODO: validate the taxid and socialsecurityid according to the current parameters for this numbers
        
        company.setName(companyDetails.getName());
        company.setTaxId(companyDetails.getTaxId());
        company.setTaxIdPassword(companyDetails.getTaxIdPassword());
        company.setSocialSecurityId(companyDetails.getSocialSecurityId());
        company.setSocialSecurityIdPassword(companyDetails.getSocialSecurityIdPassword());
        company.setAddress(companyDetails.getAddress());
        company.setPhone(companyDetails.getPhone());
        company.setEmail(companyDetails.getEmail());

        return companyRepository.save(company);
    }

    public Company getCompanyById(Integer id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
    }

    public Company getCompanyByTaxId(String taxId) {
        return companyRepository.findByTaxId(taxId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with tax ID: " + taxId));
    }

    public Company getCompanyByName(String name) {
        return companyRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with name: " + name));
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional
    public void deleteCompany(Integer id) {
        if (!companyRepository.existsById(id)) {
            throw new EntityNotFoundException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }

    private void validateNewCompany(Company company) {
        if (companyRepository.existsByName(company.getName())) {
            throw new IllegalArgumentException("Company name already exists");
        }
        
        if (companyRepository.existsByTaxId(company.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }
        
        if (companyRepository.existsBySocialSecurityId(company.getSocialSecurityId())) {
            throw new IllegalArgumentException("Social Security ID already exists");
        }
    }
} 