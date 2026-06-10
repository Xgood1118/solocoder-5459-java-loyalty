package com.loyalty.point.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {

    @NotBlank(message = "会员ID不能为空")
    private String memberId;
}
