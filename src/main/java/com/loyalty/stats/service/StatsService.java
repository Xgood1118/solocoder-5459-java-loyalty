package com.loyalty.stats.service;

import com.loyalty.common.enums.MemberLevel;
import com.loyalty.common.enums.MemberStatus;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.repository.PointTransactionRepository;
import com.loyalty.stats.dto.LevelDistribution;
import com.loyalty.stats.dto.PointStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final MemberService memberService;
    private final PointTransactionRepository transactionRepository;

    public PointStats getPointStats(LocalDateTime startTime, LocalDateTime endTime) {
        List<PointTransaction> allTx = transactionRepository.findAll();

        List<PointTransaction> filteredTx = allTx;
        if (startTime != null || endTime != null) {
            filteredTx = allTx.stream()
                    .filter(tx -> startTime == null || !tx.getCreatedAt().isBefore(startTime))
                    .filter(tx -> endTime == null || !tx.getCreatedAt().isAfter(endTime))
                    .toList();
        }

        Map<String, Integer> earnedByChannel = new LinkedHashMap<>();
        Map<String, Integer> consumedByChannel = new LinkedHashMap<>();
        int totalEarned = 0;
        int totalConsumed = 0;

        for (PointChannel channel : PointChannel.values()) {
            int earned = 0;
            int consumed = 0;
            for (PointTransaction tx : filteredTx) {
                if (tx.getChannel() == channel) {
                    if (tx.getPoints() > 0) {
                        earned += tx.getPoints();
                    } else {
                        consumed += Math.abs(tx.getPoints());
                    }
                }
            }
            if (earned > 0) {
                earnedByChannel.put(channel.getCode(), earned);
                totalEarned += earned;
            }
            if (consumed > 0) {
                consumedByChannel.put(channel.getCode(), consumed);
                totalConsumed += consumed;
            }
        }

        int totalAvailable = 0;
        int totalPermanent = 0;
        for (Member member : memberService.getAllMembers()) {
            if (!"SYSTEM_ACCOUNT".equals(member.getMemberId())) {
                totalAvailable += member.getAvailablePoints();
                totalPermanent += member.getPermanentPoints();
            }
        }

        return PointStats.builder()
                .totalEarned(totalEarned)
                .totalConsumed(totalConsumed)
                .totalAvailable(totalAvailable)
                .totalPermanent(totalPermanent)
                .earnedByChannel(earnedByChannel)
                .consumedByChannel(consumedByChannel)
                .memberCount((int) memberService.getAllMembers().stream()
                        .filter(m -> !"SYSTEM_ACCOUNT".equals(m.getMemberId()))
                        .count())
                .build();
    }

    public List<LevelDistribution> getLevelDistribution() {
        Map<MemberLevel, List<Member>> levelMap = memberService.getAllMembers().stream()
                .filter(m -> !"SYSTEM_ACCOUNT".equals(m.getMemberId()))
                .collect(Collectors.groupingBy(Member::getLevel));

        List<LevelDistribution> result = new ArrayList<>();

        for (MemberLevel level : MemberLevel.values()) {
            List<Member> members = levelMap.getOrDefault(level, Collections.emptyList());
            int count = members.size();
            int totalPts = members.stream().mapToInt(Member::getTotalPoints).sum();
            double avgPts = count > 0 ? (double) totalPts / count : 0;

            long activeCount = members.stream()
                    .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                    .filter(m -> m.getLastSignInDate() != null
                            && m.getLastSignInDate().isAfter(LocalDateTime.now().minusDays(30)))
                    .count();
            double activeRate = count > 0 ? (double) activeCount / count : 0;

            result.add(LevelDistribution.builder()
                    .levelCode(level.name())
                    .levelName(level.getName())
                    .memberCount(count)
                    .avgPoints(Math.round(avgPts * 100.0) / 100.0)
                    .activeRate(Math.round(activeRate * 10000.0) / 100.0)
                    .totalPoints(totalPts)
                    .build());
        }

        return result;
    }

    public Map<String, Object> getOverview() {
        PointStats pointStats = getPointStats(null, null);
        List<LevelDistribution> levelDist = getLevelDistribution();

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("memberCount", pointStats.getMemberCount());
        overview.put("totalAvailablePoints", pointStats.getTotalAvailable());
        overview.put("totalEarnedPoints", pointStats.getTotalEarned());
        overview.put("totalConsumedPoints", pointStats.getTotalConsumed());
        overview.put("totalPermanentPoints", pointStats.getTotalPermanent());
        overview.put("levelDistribution", levelDist);

        return overview;
    }
}
