package com.loyalty.rule.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMultiplier {

    private String categoryCode;

    private String categoryName;

    private double multiplier;
}
