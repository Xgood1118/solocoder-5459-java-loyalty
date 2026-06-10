package com.loyalty.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {

    @NotBlank(message = "转出会员ID不能为空")
    private String fromMemberId;

    @NotBlank(message = "转入会员ID不能为空")
    private String toMemberId;

    @Positive(message = "转账金额必须大于0")
    private int amount;

    private String remark;
}
