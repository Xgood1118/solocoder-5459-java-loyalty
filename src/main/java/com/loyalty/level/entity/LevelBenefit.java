package com.loyalty.level.entity;

import com.loyalty.common.enums.MemberLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelBenefit {

    private MemberLevel level;

    private double discountRate;

    private int freeShippingThreshold;

    private int birthdayGiftPoints;

    private String birthdayGiftName;

    private int pointMultiplier;
}
