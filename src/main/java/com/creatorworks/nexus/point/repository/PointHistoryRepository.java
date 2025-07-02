package com.creatorworks.nexus.point.repository;

import com.creatorworks.nexus.point.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    Page<PointHistory> findByMemberIdOrderByRegTimeDesc(Long memberId, Pageable pageable);
    
    List<PointHistory> findByMemberIdAndRelatedIdAndRelatedType(Long memberId, Long relatedId, String relatedType);
} 