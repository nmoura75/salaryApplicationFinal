package com.payroll.repository;

import com.payroll.model.EmployeeRemuneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRemunerationRepository extends JpaRepository<EmployeeRemuneration, Integer> {
    
    List<EmployeeRemuneration> findByEmployeeIdAndIsActiveTrue(Integer employeeId);
    
    List<EmployeeRemuneration> findByEmployeeIdAndCategoryIdAndIsActiveTrue(Integer employeeId, Integer categoryId);
    
    Optional<EmployeeRemuneration> findByEmployeeIdAndCategoryId(Integer employeeId, Integer categoryId);
    
    boolean existsByEmployeeIdAndCategoryId(Integer employeeId, Integer categoryId);
}
