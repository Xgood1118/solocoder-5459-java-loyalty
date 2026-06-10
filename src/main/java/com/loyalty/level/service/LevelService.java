package com.loyalty.level.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.MemberLevel;
import com.loyalty.level.entity.LevelBenefit;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LevelService {

    private final Map<MemberLevel, LevelBenefit> benefitMap = new ConcurrentHashMap<>();
    private final Map<MemberLevel, Integer> thresholdMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        thresholdMap.put(MemberLevel.NORMAL, 0);
        thresholdMap.put(MemberLevel.SILVER, 1000);
        thresholdMap.put(MemberLevel.GOLD, 5000);
        thresholdMap.put(MemberLevel.PLATINUM, 20000);

        benefitMap.put(MemberLevel.NORMAL, LevelBenefit.builder()
                .level(MemberLevel.NORMAL)
                .discountRate(1.0)
                .freeShippingThreshold(99)
                .birthdayGiftPoints(0)
                .birthdayGiftName("无")
                .pointMultiplier(1)
                .build());

        benefitMap.put(MemberLevel.SILVER, LevelBenefit.builder()
                .level(MemberLevel.SILVER)
                .discountRate(0.95)
                .freeShippingThreshold(79)
                .birthdayGiftPoints(100)
                .birthdayGiftName("银卡生日礼包")
                .pointMultiplier(1)
                .build());

        benefitMap.put(MemberLevel.GOLD, LevelBenefit.builder()
                .level(MemberLevel.GOLD)
                .discountRate(0.9)
                .freeShippingThreshold(59)
                .birthdayGiftPoints(300)
                .birthdayGiftName("金卡生日礼包")
                .pointMultiplier(1)
                .build());

        benefitMap.put(MemberLevel.PLATINUM, LevelBenefit.builder()
                .level(MemberLevel.PLATINUM)
                .discountRate(0.85)
                .freeShippingThreshold(0)
                .birthdayGiftPoints(800)
                .birthdayGiftName("白金生日礼包")
                .pointMultiplier(2)
                .build());
    }

    public LevelBenefit getBenefit(MemberLevel level) {
        LevelBenefit benefit = benefitMap.get(level);
        if (benefit == null) {
            throw new BusinessException("等级不存在: " + level);
        }
        return benefit;
    }

    public Map<MemberLevel, LevelBenefit> getAllBenefits() {
        return benefitMap.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getCode()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));
    }

    public int getThreshold(MemberLevel level) {
        return thresholdMap.getOrDefault(level, 0);
    }

    public Map<MemberLevel, Integer> getAllThresholds() {
        return thresholdMap.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getCode()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));
    }

    public void updateThreshold(MemberLevel level, int threshold) {
        thresholdMap.put(level, threshold);
        log.info("更新等级阈值: level={}, threshold={}", level.getName(), threshold);
    }

    public void updateBenefit(MemberLevel level, LevelBenefit benefit) {
        benefit.setLevel(level);
        benefitMap.put(level, benefit);
        log.info("更新等级权益: level={}", level.getName());
    }

    public MemberLevel calcLevelByPoints(int totalPoints) {
        MemberLevel result = MemberLevel.NORMAL;
        for (MemberLevel level : MemberLevel.values()) {
            if (totalPoints >= getThreshold(level)) {
                result = level;
            }
        }
        return result;
    }
}
