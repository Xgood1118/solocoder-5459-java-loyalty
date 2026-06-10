package com.loyalty.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberCreateRequest {

    @NotBlank(message = "会员姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String email;

    private LocalDate birthday;

    private String referrerId;
}
