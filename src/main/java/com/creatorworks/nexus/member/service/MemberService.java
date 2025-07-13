package com.creatorworks.nexus.member.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.member.dto.EmailAuthRequestDto;
import com.creatorworks.nexus.member.entity.EmailAuth;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.EmailAuthRepository;
import com.creatorworks.nexus.member.repository.MemberRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final JavaMailSender javaMailSender;

    public void sendAuthEmail(String email) {
        // 랜덤 인증 코드 생성 (예: UUID 사용)
        String authCode = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5); // 5분 후 만료

        EmailAuth emailAuth = new EmailAuth();
        emailAuth.setEmail(email);
        emailAuth.setAuthCode(authCode);
        emailAuth.setExpireTime(expireTime);

        emailAuthRepository.save(emailAuth);

        // 이메일 발송 로직 (실제 메일 발송)
        sendEmail(email, "회원가입 이메일 인증", "인증 코드: " + authCode);
    }
    // [2] 인증코드 확인
    public boolean verifyEmail(EmailAuthRequestDto requestDto) {
        EmailAuth emailAuth = emailAuthRepository.findById(requestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("인증 정보를 찾을 수 없습니다."));

        // 시간 만료 여부 확인 및 코드 일치 여부 확인
        if (LocalDateTime.now().isAfter(emailAuth.getExpireTime()) ||
                !emailAuth.getAuthCode().equals(requestDto.getAuthCode())) {
            return false;
        }

        // 인증 성공 시, 인증 정보 삭제
        emailAuthRepository.delete(emailAuth);
        return true;
    }

    // (실제 이메일 발송을 담당하는 private 메소드)
    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송에 실패했습니다.", e);
        }
    }
    

    /////////////////////////////login저장하는 라인//////////////////////////////////////////////////

    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        // 회원 정보 확인
        return memberRepository.save(member); // 데이터베이스에 저장을 하라는 명령
    }

    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다."); // 예외 발생
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }
        
        return new CustomUserDetails(member);
    }
}
