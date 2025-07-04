package com.creatorworks.nexus.admin.service;

import com.creatorworks.nexus.admin.entity.SellerRequest;
import com.creatorworks.nexus.admin.repository.SellerRequestRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.notification.dto.SellerRequestNotificationRequest;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerRequestService {

    private final SellerRequestRepository sellerRequestRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    // 작가 신청 생성
    public SellerRequest createSellerRequest(Member member) {
        // 이미 대기중인 신청이 있는지 확인
        if (sellerRequestRepository.existsByMemberIdAndStatus(member.getId(), SellerRequest.RequestStatus.PENDING)) {
            throw new IllegalStateException("이미 작가 신청이 진행 중입니다.");
        }

        SellerRequest sellerRequest = new SellerRequest();
        sellerRequest.setMember(member);
        sellerRequest.setStatus(SellerRequest.RequestStatus.PENDING);
        
        SellerRequest savedRequest = sellerRequestRepository.save(sellerRequest);

        // Admin에게 신청 알림 보내기
        sendAdminNotification(member, savedRequest);
        
        return savedRequest;
    }

    // Admin에게 신청 알림 보내기
    private void sendAdminNotification(Member member, SellerRequest request) {
        // 모든 Admin 사용자에게 DB 알림 저장
        List<Member> admins = memberRepository.findByRole(Role.ADMIN);
        for (Member admin : admins) {
            SellerRequestNotificationRequest notificationDto = new SellerRequestNotificationRequest();
            notificationDto.setTargetUserId(admin.getId()); // DB 저장을 위해 개별 ID 설정
            notificationDto.setMessage("새로운 작가 등록 신청이 있습니다. 신청자: " + member.getName() + " (" + member.getEmail() + ")");
            notificationDto.setType("seller_request_received");
            notificationDto.setCategory(NotificationCategory.ADMIN);
            
            notificationService.saveSellerRequestNotification(notificationDto, "/admin/seller-management");
        }

        // 실시간 알림은 관리자 그룹에 한 번만 전송
        SellerRequestNotificationRequest broadcastDto = new SellerRequestNotificationRequest();
        broadcastDto.setMessage("새로운 작가 등록 신청이 있습니다. 신청자: " + member.getName() + " (" + member.getEmail() + ")");
        broadcastDto.setType("seller_request_received");
        broadcastDto.setCategory(NotificationCategory.ADMIN);
        notificationService.sendNotificationToAdminGroup(broadcastDto);
    }

    // 대기중인 신청 목록 조회
    @Transactional(readOnly = true)
    public List<SellerRequest> getPendingRequests() {
        return sellerRequestRepository.findByStatusOrderByRegTimeDesc(SellerRequest.RequestStatus.PENDING);
    }

    // 신청 승인
    public void approveRequest(Long requestId) {
        SellerRequest request = sellerRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        if (request.getStatus() != SellerRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus(SellerRequest.RequestStatus.APPROVED);
        
        // 회원의 역할을 SELLER로 변경
        Member member = request.getMember();
        member.setRole(Role.SELLER);
        memberRepository.save(member);

        // 승인 알림 보내기
        SellerRequestNotificationRequest notificationDto = new SellerRequestNotificationRequest();
        notificationDto.setTargetUserId(member.getId());
        notificationDto.setMessage("축하합니다! 작가 등록이 승인되었습니다. 이제 작품을 등록하고 판매할 수 있습니다.");
        notificationDto.setType("seller_approved");
        notificationDto.setCategory(NotificationCategory.ADMIN);
        
        notificationService.saveSellerRequestNotification(notificationDto, "/seller/dashboard");
    }

    // 신청 거절
    public void rejectRequest(Long requestId, String reason) {
        SellerRequest request = sellerRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        if (request.getStatus() != SellerRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus(SellerRequest.RequestStatus.REJECTED);
        request.setReason(reason);

        // 거절 알림 보내기
        SellerRequestNotificationRequest notificationDto = new SellerRequestNotificationRequest();
        notificationDto.setTargetUserId(request.getMember().getId());
        notificationDto.setMessage("작가 등록 신청이 거절되었습니다. 거절 사유: " + reason);
        notificationDto.setType("seller_rejected");
        notificationDto.setReason(reason);
        notificationDto.setCategory(NotificationCategory.ADMIN);
        
        notificationService.saveSellerRequestNotification(notificationDto, "/User/my-page");
    }
} 