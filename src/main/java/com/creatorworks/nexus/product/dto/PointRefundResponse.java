package com.creatorworks.nexus.product.dto;

import com.creatorworks.nexus.product.entity.PointRefund;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRefundResponse {
    
    private Long id;
    private Long amount;
    private String reason;
    private String status;
    private String statusDescription;
    private String refundUid;
    private String adminComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PointRefundResponse from(PointRefund refund) {
        return PointRefundResponse.builder()
                .id(refund.getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus().name())
                .statusDescription(refund.getStatus().getDescription())
                .refundUid(refund.getRefundUid())
                .adminComment(refund.getAdminComment())
                .createdAt(refund.getRegTime())
                .updatedAt(refund.getUpdateTime())
                .build();
    }
} 