package com.creatorworks.nexus.member.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.creatorworks.nexus.member.dto.MemberFormDto;
import com.creatorworks.nexus.member.dto.MemberModifyDto;
import jakarta.persistence.EntityNotFoundException;
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
        System.out.println(member.getEmail());
        System.out.println(member.getPassword());
        System.out.println(member.getGender());
        System.out.println(member.getBirthYear());
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


    /**
     * 이메일로 회원 정보를 조회하는 메소드 (수정 폼 데이터 로딩 시 사용)
     * @param email 조회할 사용자의 이메일
     * @return 조회된 Member 엔티티
     */
    public Member findByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email);
        }
        return member;
    }

    /**
     * 회원 정보 업데이트 메소드
     * @param memberModifyDto 사용자가 수정한 정보가 담긴 DTO
     * @param email 현재 로그인된 사용자의 이메일 (수정할 대상을 식별)
     */
    public void updateMember(MemberModifyDto memberModifyDto, String email) {
        // 1. 현재 로그인된 사용자의 이메일로 DB에서 영속 상태의 Member 엔티티를 조회합니다.
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("수정할 회원 정보를 찾을 수 없습니다.");
        }

        // 2. (선택적) 별명 중복 검증: 다른 사람이 이미 사용 중인 별명인지 확인합니다.
        // 현재 사용자의 기존 별명과 다른 새로운 별명을 입력했을 때만 중복 검사를 수행합니다.
        if (!member.getName().equals(memberModifyDto.getName())) {
            validateDuplicateName(memberModifyDto.getName());
        }

        // 3. DTO의 값으로 Member 엔티티의 필드를 업데이트합니다. (비밀번호 제외)
        //    @Transactional에 의해 메소드 종료 시 변경 감지(Dirty Checking)가 일어나 DB에 UPDATE 쿼리가 실행됩니다.
        member.updateProfile(
                memberModifyDto.getName(),
                memberModifyDto.getGender(),
                memberModifyDto.getBirthYear(),
                memberModifyDto.getBirthMonth(),
                memberModifyDto.getBirthDay()
        );

        // memberRepository.save(member); // @Transactional 환경에서는 생략 가능
    }

    /**
     * 별명(활동명) 중복을 검증하는 private 메소드
     * @param name 검증할 별명
     */
    private void validateDuplicateName(String name) {
        // MemberRepository에 findByName 메소드가 구현되어 있어야 합니다.
        Member findMember = memberRepository.findByName(name);
        if (findMember != null) {
            throw new IllegalStateException("이미 사용 중인 별명입니다.");
        }
    }

}
