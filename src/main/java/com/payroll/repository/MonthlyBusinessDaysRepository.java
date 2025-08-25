package com.payroll.repository;

import com.payroll.model.MonthlyBusinessDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyBusinessDaysRepository extends JpaRepository<MonthlyBusinessDays, Integer> {
    
    Optional<MonthlyBusinessDays> findByCompanyIdAndYearAndMonth(Integer companyId, Integer year, Integer month);
    
    List<MonthlyBusinessDays> findByCompanyIdAndYearOrderByMonthAsc(Integer companyId, Integer year);
    
    @Query("SELECT mbd FROM MonthlyBusinessDays mbd WHERE mbd.companyId = :companyId " +
           "AND (mbd.year > :year OR (mbd.year = :year AND mbd.month >= :month)) " +
           "ORDER BY mbd.year ASC, mbd.month ASC")
    List<MonthlyBusinessDays> findFromMonthOnwards(@Param("companyId") Integer companyId, 
                                                   @Param("year") Integer year, 
                                                   @Param("month") Integer month);
    
    boolean existsByCompanyIdAndYearAndMonth(Integer companyId, Integer year, Integer month);
}
