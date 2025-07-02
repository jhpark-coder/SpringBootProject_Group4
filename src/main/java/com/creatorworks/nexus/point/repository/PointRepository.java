package com.creatorworks.nexus.point.repository;

import com.creatorworks.nexus.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    
    Optional<Point> findByMemberId(Long memberId);
    
    boolean existsByMemberId(Long memberId);
} 