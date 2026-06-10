package com.loyalty.point.entity;

import com.loyalty.common.enums.PointChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction {

    private Long transactionId;

    private String memberId;

    private int points;

    private int balanceAfter;

    private PointChannel channel;

    private String description;

    private String orderId;

    private String activityId;

    private String referralId;

    private String reviewId;

    private String couponId;

    private String goodsId;

    private String lotteryId;

    private String donationOrgId;

    private boolean permanent;

    private LocalDateTime createdAt;
}
