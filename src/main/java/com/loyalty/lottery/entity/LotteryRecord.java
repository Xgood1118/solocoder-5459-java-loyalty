package com.loyalty.lottery.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryRecord {

    private String recordId;

    private String memberId;

    private int costPoints;

    private String prizeId;

    private String prizeName;

    private String prizeType;

    private int prizeValue;

    private boolean won;

    private String status;

    private LocalDateTime createdAt;
}
