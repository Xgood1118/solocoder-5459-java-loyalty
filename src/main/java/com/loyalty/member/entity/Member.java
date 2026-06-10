package com.loyalty.member.entity;

import com.loyalty.common.enums.MemberLevel;
import com.loyalty.common.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    private String memberId;

    private String name;

    private String phone;

    private String email;

    private LocalDate birthday;

    private MemberLevel level;

    private MemberStatus status;

    private int totalPoints;

    private int availablePoints;

    private int frozenPoints;

    private int permanentPoints;

    private int yearEarnedPoints;

    private boolean profileCompleted;

    private String referrerId;

    private boolean refereeRewardGiven;

    private LocalDateTime lastSignInDate;

    private int signInDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private long version;
}
