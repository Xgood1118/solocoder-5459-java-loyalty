package com.loyalty.rule.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.ProductCategory;
import com.loyalty.rule.entity.ActivityRule;
import com.loyalty.rule.entity.CategoryMultiplier;
import com.loyalty.rule.entity.SignInRule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RuleService {

    private final Map<String, CategoryMultiplier> categoryMultipliers = new ConcurrentHashMap<>();
    private final Map<String, ActivityRule> activityRules = new ConcurrentHashMap<>();
    private SignInRule signInRule;
    private static final int MEMBER_DAY = 15;

    @PostConstruct
    public void init() {
        for (ProductCategory cat : ProductCategory.values()) {
            categoryMultipliers.put(cat.getCode(), CategoryMultiplier.builder()
                    .categoryCode(cat.getCode())
                    .categoryName(cat.getName())
                    .multiplier(cat.getMultiplier())
                    .build());
        }

        signInRule = SignInRule.builder()
                .dailyPoints(5)
                .enable(true)
                .build();
    }

    public Collection<CategoryMultiplier> getAllCategoryMultipliers() {
        return categoryMultipliers.values();
    }

    public CategoryMultiplier getCategoryMultiplier(String categoryCode) {
        CategoryMultiplier m = categoryMultipliers.get(categoryCode);
        if (m == null) {
            throw new BusinessException("品类不存在: " + categoryCode);
        }
        return m;
    }

    public double getCategoryMultiplierValue(String categoryCode) {
        CategoryMultiplier m = categoryMultipliers.get(categoryCode);
        return m != null ? m.getMultiplier() : 1.0;
    }

    public void updateCategoryMultiplier(String categoryCode, double multiplier) {
        CategoryMultiplier m = getCategoryMultiplier(categoryCode);
        m.setMultiplier(multiplier);
        log.info("更新品类倍率: category={}, multiplier={}", categoryCode, multiplier);
    }

    public SignInRule getSignInRule() {
        return signInRule;
    }

    public void updateSignInRule(SignInRule rule) {
        this.signInRule = rule;
        log.info("更新签到规则: dailyPoints={}", rule.getDailyPoints());
    }

    public ActivityRule createActivity(ActivityRule activity) {
        String activityId = "ACT" + System.currentTimeMillis();
        activity.setActivityId(activityId);
        activityRules.put(activityId, activity);
        log.info("创建活动: activityId={}, name={}", activityId, activity.getName());
        return activity;
    }

    public ActivityRule getActivity(String activityId) {
        ActivityRule activity = activityRules.get(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在: " + activityId);
        }
        return activity;
    }

    public Collection<ActivityRule> getAllActivities() {
        return activityRules.values();
    }

    public void updateActivity(String activityId, ActivityRule activity) {
        if (!activityRules.containsKey(activityId)) {
            throw new BusinessException("活动不存在: " + activityId);
        }
        activity.setActivityId(activityId);
        activityRules.put(activityId, activity);
        log.info("更新活动: activityId={}", activityId);
    }

    public void deleteActivity(String activityId) {
        if (!activityRules.containsKey(activityId)) {
            throw new BusinessException("活动不存在: " + activityId);
        }
        activityRules.remove(activityId);
        log.info("删除活动: activityId={}", activityId);
    }

    public boolean isActivityValid(String activityId, LocalDateTime time) {
        ActivityRule activity = activityRules.get(activityId);
        if (activity == null || !activity.isEnabled()) {
            return false;
        }
        if (activity.getStartTime() != null && time.isBefore(activity.getStartTime())) {
            return false;
        }
        if (activity.getEndTime() != null && time.isAfter(activity.getEndTime())) {
            return false;
        }
        return true;
    }

    public int getMemberDay() {
        return MEMBER_DAY;
    }
}
