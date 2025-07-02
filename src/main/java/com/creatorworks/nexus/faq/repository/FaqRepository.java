package com.creatorworks.nexus.faq.repository;

import com.creatorworks.nexus.faq.entity.Faq;
import com.creatorworks.nexus.faq.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    
    List<Faq> findByCategoryAndIsActiveTrueOrderBySortOrderAsc(FaqCategory category);
    
    List<Faq> findByIsActiveTrueOrderBySortOrderAsc();
    
    @Query("SELECT f FROM Faq f WHERE f.isActive = true AND " +
           "(LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY f.sortOrder ASC")
    List<Faq> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT f FROM Faq f WHERE f.isActive = true " +
           "ORDER BY f.viewCount DESC, f.sortOrder ASC " +
           "LIMIT 10")
    List<Faq> findTop10ByViewCount();
} 