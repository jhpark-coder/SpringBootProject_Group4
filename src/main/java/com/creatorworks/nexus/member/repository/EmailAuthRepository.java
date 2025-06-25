package com.creatorworks.nexus.member.repository;

import com.creatorworks.nexus.member.entity.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAuthRepository extends JpaRepository<EmailAuth, String> {
}
