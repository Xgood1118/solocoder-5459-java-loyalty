package com.loyalty.lottery.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.lottery.entity.LotteryRecord;
import com.loyalty.lottery.entity.Prize;
import com.loyalty.lottery.repository.LotteryRecordRepository;
import com.loyalty.lottery.repository.PrizeRepository;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.repository.PointTransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryService {

    private final PrizeRepository prizeRepository;
    private final LotteryRecordRepository lotteryRecordRepository;
    private final MemberService memberService;
    private final PointTransactionRepository transactionRepository;

    private static final int LOTTERY_COST = 100;

    @PostConstruct
    public void initDefaultPrizes() {
        if (prizeRepository.findAll().isEmpty()) {
            addPrize(Prize.builder()
                    .prizeId("PRIZE_NONE")
                    .name("谢谢参与")
                    .type("none")
                    .value(0)
                    .probability(0.6)
                    .quantity(-1)
                    .claimed(0)
                    .enabled(true)
                    .build());

            addPrize(Prize.builder()
                    .prizeId("PRIZE_POINTS_50")
                    .name("50积分")
                    .type("points")
                    .value(50)
                    .probability(0.2)
                    .quantity(-1)
                    .claimed(0)
                    .enabled(true)
                    .build());

            addPrize(Prize.builder()
                    .prizeId("PRIZE_POINTS_200")
                    .name("200积分")
                    .type("points")
                    .value(200)
                    .probability(0.1)
                    .quantity(-1)
                    .claimed(0)
                    .enabled(true)
                    .build());

            addPrize(Prize.builder()
                    .prizeId("PRIZE_COUPON_50")
                    .name("50元优惠券")
                    .type("coupon")
                    .value(50)
                    .probability(0.07)
                    .quantity(100)
                    .claimed(0)
                    .enabled(true)
                    .build());

            addPrize(Prize.builder()
                    .prizeId("PRIZE_GIFT_BOX")
                    .name("精美礼盒")
                    .type("goods")
                    .value(200)
                    .probability(0.03)
                    .quantity(20)
                    .claimed(0)
                    .enabled(true)
                    .build());
        }
    }

    public Prize addPrize(Prize prize) {
        if (prize.getPrizeId() == null) {
            prize.setPrizeId("PRIZE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        return prizeRepository.save(prize);
    }

    public Collection<Prize> listPrizes() {
        return prizeRepository.findAll();
    }

    public Prize updatePrize(String prizeId, Prize prize) {
        Prize existing = prizeRepository.findById(prizeId);
        if (existing == null) {
            throw new BusinessException("奖品不存在: " + prizeId);
        }
        prize.setPrizeId(prizeId);
        return prizeRepository.save(prize);
    }

    public void deletePrize(String prizeId) {
        if (prizeRepository.findById(prizeId) == null) {
            throw new BusinessException("奖品不存在: " + prizeId);
        }
        prizeRepository.deleteById(prizeId);
    }

    public LotteryRecord draw(String memberId) {
        Member member = memberService.getMember(memberId);
        if (!member.getStatus().canConsume()) {
            throw new BusinessException("会员状态异常，无法抽奖");
        }
        if (member.getAvailablePoints() < LOTTERY_COST) {
            throw new BusinessException("积分不足，抽奖需要 " + LOTTERY_COST + " 积分");
        }

        memberService.getMemberLock(memberId).lock();
        try {
            Member m = memberService.getMember(memberId);
            if (m.getAvailablePoints() < LOTTERY_COST) {
                throw new BusinessException("积分不足");
            }

            memberService.subtractPoints(memberId, LOTTERY_COST, PointChannel.LOTTERY);

            boolean success = false;
            LotteryRecord record = null;
            try {
                Prize prize = drawPrize();
                boolean won = !"none".equals(prize.getType());

                String recordId = "LOT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                record = LotteryRecord.builder()
                        .recordId(recordId)
                        .memberId(memberId)
                        .costPoints(LOTTERY_COST)
                        .prizeId(prize.getPrizeId())
                        .prizeName(prize.getName())
                        .prizeType(prize.getType())
                        .prizeValue(prize.getValue())
                        .won(won)
                        .status("SUCCESS")
                        .createdAt(LocalDateTime.now())
                        .build();

                if (won && "points".equals(prize.getType())) {
                    memberService.addPoints(memberId, prize.getValue(), false, PointChannel.LOTTERY);
                }

                if (won && prize.getQuantity() > 0) {
                    prize.setClaimed(prize.getClaimed() + 1);
                    if (prize.getClaimed() >= prize.getQuantity()) {
                        prize.setEnabled(false);
                    }
                    prizeRepository.save(prize);
                }

                lotteryRecordRepository.save(record);

                PointTransaction costTx = PointTransaction.builder()
                        .memberId(memberId)
                        .points(-LOTTERY_COST)
                        .channel(PointChannel.LOTTERY)
                        .description("抽奖消耗")
                        .lotteryId(recordId)
                        .permanent(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                Member after = memberService.getMember(memberId);
                costTx.setBalanceAfter(after.getAvailablePoints());
                transactionRepository.save(costTx);

                if (won && "points".equals(prize.getType())) {
                    PointTransaction winTx = PointTransaction.builder()
                            .memberId(memberId)
                            .points(prize.getValue())
                            .channel(PointChannel.LOTTERY)
                            .description("抽奖中奖 - " + prize.getName())
                            .lotteryId(recordId)
                            .permanent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    Member afterWin = memberService.getMember(memberId);
                    winTx.setBalanceAfter(afterWin.getAvailablePoints());
                    transactionRepository.save(winTx);
                }

                success = true;
                log.info("抽奖完成: memberId={}, recordId={}, won={}, prize={}",
                        memberId, recordId, won, prize.getName());
                return record;
            } catch (Exception e) {
                log.error("抽奖过程异常，回滚积分扣除", e);
                throw e;
            } finally {
                if (!success) {
                    memberService.addPoints(memberId, LOTTERY_COST, false, PointChannel.LOTTERY);
                    log.info("抽奖失败，积分已回滚: memberId={}, points={}", memberId, LOTTERY_COST);
                }
            }
        } finally {
            memberService.getMemberLock(memberId).unlock();
        }
    }

    private Prize drawPrize() {
        List<Prize> prizes = prizeRepository.findEnabled().stream()
                .filter(p -> p.getQuantity() < 0 || p.getClaimed() < p.getQuantity())
                .toList();

        if (prizes.isEmpty()) {
            throw new BusinessException("奖品池为空");
        }

        double totalProb = prizes.stream().mapToDouble(Prize::getProbability).sum();
        if (totalProb <= 0) {
            throw new BusinessException("奖品概率配置错误");
        }

        double rand = ThreadLocalRandom.current().nextDouble() * totalProb;
        double cumulative = 0;
        for (Prize prize : prizes) {
            cumulative += prize.getProbability();
            if (rand <= cumulative) {
                return prize;
            }
        }
        return prizes.get(prizes.size() - 1);
    }

    public List<LotteryRecord> listRecords(String memberId) {
        if (memberId != null) {
            return lotteryRecordRepository.findByMemberId(memberId);
        }
        return lotteryRecordRepository.findAll();
    }

    public LotteryRecord getRecord(String recordId) {
        LotteryRecord record = lotteryRecordRepository.findById(recordId);
        if (record == null) {
            throw new BusinessException("抽奖记录不存在: " + recordId);
        }
        return record;
    }
}
