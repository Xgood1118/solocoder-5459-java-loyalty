package com.loyalty.redemption.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    private String couponTemplateId;

    private String name;

    private int faceValue;

    private int pointsRequired;

    private int totalCount;

    private int issuedCount;

    private int validDays;

    private boolean enabled;
}
