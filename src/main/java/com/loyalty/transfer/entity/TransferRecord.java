package com.loyalty.transfer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRecord {

    private String transferId;

    private String fromMemberId;

    private String toMemberId;

    private int amount;

    private int fee;

    private int receivedAmount;

    private String status;

    private String remark;

    private LocalDateTime createdAt;
}
