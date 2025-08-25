package com.payroll.repository;

import com.payroll.model.MonthlyRemuneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyRemunerationRepository extends JpaRepository<MonthlyRemuneration, Integer> {
    
    List<MonthlyRemuneration> findByEmployeeIdAndYearAndMonth(Integer employeeId, Integer year, Integer month);
    
    Optional<MonthlyRemuneration> findByEmployeeIdAndCategoryIdAndYearAndMonth(
        Integer employeeId, Integer categoryId, Integer year, Integer month);
    
    boolean existsByEmployeeIdAndCategoryIdAndYearAndMonth(Integer employeeId, Integer categoryId, Integer year, Integer month);
}
