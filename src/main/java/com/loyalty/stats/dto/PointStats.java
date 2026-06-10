package com.loyalty.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointStats {

    private int totalEarned;

    private int totalConsumed;

    private int totalAvailable;

    private int totalPermanent;

    private Map<String, Integer> earnedByChannel;

    private Map<String, Integer> consumedByChannel;

    private int memberCount;
}
