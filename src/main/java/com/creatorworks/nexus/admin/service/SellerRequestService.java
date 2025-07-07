package com.creatorworks.nexus.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.admin.entity.SellerRequest;
import com.creatorworks.nexus.admin.repository.SellerRequestRepository;
import com.creatorworks.nexus.member.constant.Role;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.dto.FollowNotificationRequest;
import com.creatorworks.nexus.notification.dto.SellerRequestNotificationRequest;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

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
        
        // Admin에게 신청 알림 보내기
        sendAdminNotification(member, sellerRequest);
        
        // 저장 호출 위치 추적
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2) {
            StackTraceElement caller = stackTrace[2];
            System.out.println("=== SellerRequest 저장 호출 ===");
            System.out.println("호출 위치: " + caller.getClassName() + "." + caller.getMethodName() +
                               " (" + caller.getFileName() + ":" + caller.getLineNumber() + ")");
        }
        return sellerRequestRepository.save(sellerRequest);
    }

    // Admin에게 신청 알림 보내기
    private void sendAdminNotification(Member member, SellerRequest request) {
        // 1. 관리자용 알림 저장 (targetUserId = 0)
        SellerRequestNotificationRequest adminNotificationDto = new SellerRequestNotificationRequest();
        adminNotificationDto.setTargetUserId(0L); // 관리자 알림
        adminNotificationDto.setMessage("새로운 작가 등록 신청이 있습니다. 신청자: " + member.getName() + " (" + member.getEmail() + ")");
        adminNotificationDto.setType("seller_request_received");
        adminNotificationDto.setCategory(NotificationCategory.ADMIN);
        notificationService.saveSellerRequestNotification(adminNotificationDto, "/admin/seller-management");

        // 2. 신청자용 알림 저장 (targetUserId = 신청자 ID)
        SellerRequestNotificationRequest userNotificationDto = new SellerRequestNotificationRequest();
        userNotificationDto.setTargetUserId(member.getId()); // 신청자 알림
        userNotificationDto.setMessage("작가 등록 신청이 접수되었습니다. 검토까지 시간이 소요될 수 있습니다.");
        userNotificationDto.setType("seller_request_submitted");
        userNotificationDto.setCategory(NotificationCategory.ADMIN);
        notificationService.saveSellerRequestNotification(userNotificationDto, null);

        // 2-1. 신청자에게 실시간 알림 전송
        FollowNotificationRequest userFollowDto = new FollowNotificationRequest();
        userFollowDto.setTargetUserId(member.getId());
        userFollowDto.setSenderUserId(0L); // 시스템 알림
        userFollowDto.setMessage("작가 등록 신청이 접수되었습니다. 검토까지 시간이 소요될 수 있습니다.");
        userFollowDto.setType("seller_request_submitted");
        userFollowDto.setCategory(NotificationCategory.ADMIN);
        notificationService.sendNotification(userFollowDto);

        // 3. 관리자 그룹에 실시간 알림 전송용
        SellerRequestNotificationRequest broadcastDto = new SellerRequestNotificationRequest();
        broadcastDto.setMessage("새로운 작가 등록 신청이 있습니다. 신청자: " + member.getName() + " (" + member.getEmail() + ")");
        broadcastDto.setType("seller_request_received");
        broadcastDto.setCategory(NotificationCategory.ADMIN);
        // 실시간 알림 전송 (NestJS 서버에서 추가 저장하지 않도록 수정 필요)
        notificationService.sendNotificationToAdminGroup(broadcastDto);
    }

    // 대기중인 신청 목록 조회
    @Transactional(readOnly = true)
    public List<SellerRequest> getPendingRequests() {
        return sellerRequestRepository.findByStatusOrderByRegTimeDesc(SellerRequest.RequestStatus.PENDING);
    }

    // 특정 회원의 최신 작가신청 조회
    @Transactional(readOnly = true)
    public SellerRequest getLatestSellerRequest(Long memberId) {
        List<SellerRequest> requests = sellerRequestRepository.findByMemberIdOrderByRegTimeDesc(memberId);
        return requests.isEmpty() ? null : requests.get(0);
    }

    // 신청 승인
    public void approveRequest(Long requestId) {
        SellerRequest sellerRequest = sellerRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        if (sellerRequest.getStatus() != SellerRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        sellerRequest.setStatus(SellerRequest.RequestStatus.APPROVED);
        
        Member member = sellerRequest.getMember();
        member.setRole(Role.SELLER);
        memberRepository.save(member);

        // 승인 알림 보내기
        FollowNotificationRequest notificationDto = new FollowNotificationRequest();
        notificationDto.setTargetUserId(member.getId());
        notificationDto.setSenderUserId(0L); // 시스템 알림
        notificationDto.setMessage("축하합니다! 작가 등록이 승인되었습니다.");
        notificationDto.setType("seller_approved");
        notificationDto.setCategory(NotificationCategory.ADMIN);
        
        // DB 저장과 실시간 알림 전송
        notificationService.saveNotification(notificationDto, "/seller/dashboard");
        notificationService.sendNotification(notificationDto);
    }

    // 신청 거절
    public void rejectRequest(Long requestId, String reason) {
        SellerRequest sellerRequest = sellerRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));

        if (sellerRequest.getStatus() != SellerRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        sellerRequest.setStatus(SellerRequest.RequestStatus.REJECTED);
        sellerRequest.setReason(reason);

        // 거절 알림 보내기
        FollowNotificationRequest notificationDto = new FollowNotificationRequest();
        notificationDto.setTargetUserId(sellerRequest.getMember().getId());
        notificationDto.setSenderUserId(0L); // 시스템 알림
        notificationDto.setMessage("작가 등록 신청이 거절되었습니다. 사유: " + reason);
        notificationDto.setType("seller_rejected");
        notificationDto.setCategory(NotificationCategory.ADMIN);
        
        // DB 저장과 실시간 알림 전송
        notificationService.saveNotification(notificationDto, "/members/seller-register");
        notificationService.sendNotification(notificationDto);
    }
} 