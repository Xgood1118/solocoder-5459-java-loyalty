package com.loyalty.expire.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpireRecord {

    private String recordId;

    private int year;

    private int affectedMembers;

    private int totalExpiredPoints;

    private LocalDateTime executedAt;

    private String status;

    private String remark;
}
