package com.payroll.repository;

import com.payroll.model.RemunerationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemunerationCategoryRepository extends JpaRepository<RemunerationCategory, Integer> {
    
    List<RemunerationCategory> findByCompanyIdOrderBySortOrderAsc(Integer companyId);
    
    List<RemunerationCategory> findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(Integer companyId);
    
    Optional<RemunerationCategory> findByNameAndCompanyId(String name, Integer companyId);
    
    boolean existsByNameAndCompanyId(String name, Integer companyId);
    
    @Query("SELECT rc FROM RemunerationCategory rc WHERE rc.companyId = :companyId AND rc.type = :type ORDER BY rc.sortOrder")
    List<RemunerationCategory> findByCompanyIdAndTypeOrderBySortOrder(@Param("companyId") Integer companyId, @Param("type") RemunerationCategory.RemunerationType type);
}
