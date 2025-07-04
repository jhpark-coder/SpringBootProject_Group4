package com.creatorworks.nexus.member.repository;

import com.creatorworks.nexus.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    Member findByName(String name);
}
