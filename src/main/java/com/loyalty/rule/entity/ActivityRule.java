package com.loyalty.rule.entity;

import com.loyalty.common.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRule {

    private String activityId;

    private String name;

    private String description;

    private double pointMultiplier;

    private boolean permanentPoints;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean enabled;

    private int maxPointsPerMember;

    private ProductCategory applicableCategory;
}
