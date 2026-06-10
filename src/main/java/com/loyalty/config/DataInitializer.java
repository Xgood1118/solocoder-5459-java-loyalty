package com.loyalty.config;

import com.loyalty.common.enums.MemberLevel;
import com.loyalty.common.enums.MemberStatus;
import com.loyalty.member.entity.Member;
import com.loyalty.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final MemberRepository memberRepository;

    public static final String SYSTEM_MEMBER_ID = "SYSTEM_ACCOUNT";

    @PostConstruct
    public void init() {
        if (memberRepository.findById(SYSTEM_MEMBER_ID) == null) {
            Member system = Member.builder()
                    .memberId(SYSTEM_MEMBER_ID)
                    .name("系统账户")
                    .phone("00000000000")
                    .email("system@loyalty.com")
                    .level(MemberLevel.PLATINUM)
                    .status(MemberStatus.ACTIVE)
                    .totalPoints(0)
                    .availablePoints(0)
                    .frozenPoints(0)
                    .permanentPoints(0)
                    .yearEarnedPoints(0)
                    .profileCompleted(true)
                    .refereeRewardGiven(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .version(0)
                    .build();
            memberRepository.save(system);
            log.info("系统账户初始化完成: {}", SYSTEM_MEMBER_ID);
        }
    }
}
