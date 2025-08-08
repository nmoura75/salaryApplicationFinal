package com.payroll.repository;

import com.payroll.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    
    Optional<Company> findByName(String name);
    
    Optional<Company> findByTaxId(String taxId);
    
    Optional<Company> findBySocialSecurityId(String socialSecurityId);
    
    boolean existsByTaxId(String taxId);
    
    boolean existsBySocialSecurityId(String socialSecurityId);
    
    boolean existsByName(String name);
} 