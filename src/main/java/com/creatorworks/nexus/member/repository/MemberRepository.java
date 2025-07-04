package com.creatorworks.nexus.member.repository;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.constant.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    List<Member> findByRole(Role role);
}
