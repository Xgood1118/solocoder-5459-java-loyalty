package com.loyalty.redemption.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRecord {

    private String couponId;

    private String templateId;

    private String memberId;

    private String name;

    private int faceValue;

    private String status;

    private java.time.LocalDateTime issuedAt;

    private java.time.LocalDateTime expireAt;
}
