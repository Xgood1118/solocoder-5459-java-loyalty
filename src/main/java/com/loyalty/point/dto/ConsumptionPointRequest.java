package com.loyalty.point.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsumptionPointRequest {

    @NotBlank(message = "会员ID不能为空")
    private String memberId;

    @NotBlank(message = "订单号不能为空")
    private String orderId;

    @Positive(message = "订单金额必须大于0")
    private double amount;

    private String category;

    private String activityId;

    private LocalDateTime orderTime;
}
