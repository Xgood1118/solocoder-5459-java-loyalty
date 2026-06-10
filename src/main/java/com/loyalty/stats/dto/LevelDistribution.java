package com.loyalty.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelDistribution {

    private String levelCode;

    private String levelName;

    private int memberCount;

    private double avgPoints;

    private double activeRate;

    private int totalPoints;
}
