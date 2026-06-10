package com.loyalty.member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberUpdateRequest {

    private String name;

    private String phone;

    private String email;

    private LocalDate birthday;
}
