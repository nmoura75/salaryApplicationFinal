package com.payroll.repository;

import com.payroll.model.SalaryReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryReceiptRepository extends JpaRepository<SalaryReceipt, Integer> {

    @Query("SELECT sr FROM SalaryReceipt sr WHERE sr.employee.firstName = :firstName")
    Optional<SalaryReceipt> findByEmployeeFirstName(@Param("firstName") String firstName);
    
    List<SalaryReceipt> findByEmployeeId(Integer employeeId);
    
    Page<SalaryReceipt> findByEmployeeId(Integer employeeId, Pageable pageable);
    
    @Query("SELECT sr FROM SalaryReceipt sr WHERE sr.employee.company.id = :companyId")
    List<SalaryReceipt> findByCompanyId(@Param("companyId") Integer companyId);
    
    @Query("SELECT sr FROM SalaryReceipt sr WHERE sr.employee.company.id = :companyId")
    Page<SalaryReceipt> findByCompanyId(@Param("companyId") Integer companyId, Pageable pageable);
    
    Optional<SalaryReceipt> findByEmployeeIdAndMonthAndYear(Integer employeeId, String month, Integer year);
    
    @Query("SELECT sr FROM SalaryReceipt sr WHERE sr.employee.company.id = :companyId " +
           "AND sr.month = :month AND sr.year = :year")
    List<SalaryReceipt> findByCompanyIdAndMonthAndYear(
            @Param("companyId") Integer companyId,
            @Param("month") String month,
            @Param("year") Integer year);
    
    @Query("SELECT sr FROM SalaryReceipt sr WHERE sr.employee.company.id = :companyId " +
           "AND sr.paymentDate BETWEEN :startDate AND :endDate")
    List<SalaryReceipt> findByCompanyIdAndPaymentDateBetween(
            @Param("companyId") Integer companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    boolean existsByEmployeeIdAndMonthAndYear(Integer employeeId, String month, Integer year);
} 