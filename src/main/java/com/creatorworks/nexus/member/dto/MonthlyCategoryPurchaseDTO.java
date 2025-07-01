package com.creatorworks.nexus.member.dto;

public record MonthlyCategoryPurchaseDTO(Integer year, Integer month, String primaryCategory, Long count) {
}