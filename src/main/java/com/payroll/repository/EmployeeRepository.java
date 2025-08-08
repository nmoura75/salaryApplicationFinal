package com.payroll.repository;

import com.payroll.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    
    Optional<Employee> findByFirstName(String firstName);

    Optional<Employee> findByTaxId(String taxId);
    
    Optional<Employee> findBySocialSecurityId(String socialSecurityId);
    
    List<Employee> findByCompanyId(Integer companyId);
    
    Page<Employee> findByCompanyId(Integer companyId, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId " +
           "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.taxId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Employee> searchByCompany(@Param("companyId") Integer companyId,
                                 @Param("searchTerm") String searchTerm,
                                 Pageable pageable);
    
    boolean existsByTaxId(String taxId);
    
    boolean existsBySocialSecurityId(String socialSecurityId);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);
} 