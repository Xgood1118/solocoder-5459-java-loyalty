package com.loyalty.rule.service;

import com.loyalty.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointCalculator {

    private final RuleService ruleService;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    public int calculateConsumptionPoints(Member member, double amount, String categoryCode,
                                          LocalDateTime orderTime, String activityId) {
        if (amount <= 0) {
            return 0;
        }

        double basePoints = Math.floor(amount);

        double categoryMultiplier = ruleService.getCategoryMultiplierValue(categoryCode);
        if (categoryMultiplier <= 0) {
            return 0;
        }

        double finalMultiplier = categoryMultiplier;

        LocalDate orderDate = orderTime.atZone(DEFAULT_ZONE).toLocalDate();

        if (isBirthdayMonth(member, orderDate)) {
            finalMultiplier *= 2;
            log.debug("生日月双倍积分, memberId={}", member.getMemberId());
        }

        if (isMemberDay(orderDate)) {
            finalMultiplier *= 3;
            log.debug("会员日三倍积分, date={}", orderDate);
        }

        if (activityId != null && ruleService.isActivityValid(activityId, orderTime)) {
            var activity = ruleService.getActivity(activityId);
            if (activity.getApplicableCategory() == null
                    || activity.getApplicableCategory().getCode().equals(categoryCode)) {
                finalMultiplier *= activity.getPointMultiplier();
                log.debug("活动倍率叠加, activityId={}, multiplier={}", activityId, activity.getPointMultiplier());
            }
        }

        int points = (int) Math.floor(basePoints * finalMultiplier);
        log.debug("积分计算: amount={}, category={}, multiplier={}, points={}",
                amount, categoryCode, finalMultiplier, points);
        return points;
    }

    public boolean isBirthdayMonth(Member member, LocalDate date) {
        if (member.getBirthday() == null) {
            return false;
        }
        MonthDay birthday = MonthDay.from(member.getBirthday());
        MonthDay today = MonthDay.from(date);
        return birthday.getMonth() == today.getMonth();
    }

    public boolean isMemberDay(LocalDate date) {
        return date.getDayOfMonth() == ruleService.getMemberDay();
    }

    public boolean isPermanentByChannel(String channel) {
        return "referee".equals(channel) || "referred".equals(channel) || "activity_permanent".equals(channel);
    }
}
