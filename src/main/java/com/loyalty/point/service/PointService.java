package com.loyalty.point.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.common.enums.ProductCategory;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import com.loyalty.point.dto.ConsumptionPointRequest;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.repository.PointTransactionRepository;
import com.loyalty.rule.service.PointCalculator;
import com.loyalty.rule.service.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointTransactionRepository transactionRepository;
    private final MemberService memberService;
    private final PointCalculator pointCalculator;
    private final RuleService ruleService;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int PROFILE_COMPLETE_POINTS = 50;
    private static final int REFEREE_POINTS = 100;
    private static final int REFERRED_POINTS = 100;
    private static final int REVIEW_POINTS = 10;

    private final Set<String> reviewIndex = ConcurrentHashMap.newKeySet();

    public PointTransaction earnByConsumption(ConsumptionPointRequest request) {
        if (transactionRepository.existsByOrderId(request.getOrderId())) {
            log.info("订单已入账，幂等跳过: orderId={}", request.getOrderId());
            return transactionRepository.findByOrderId(request.getOrderId());
        }

        Member member = memberService.getMember(request.getMemberId());
        String category = request.getCategory() != null ? request.getCategory() : ProductCategory.NORMAL.getCode();
        LocalDateTime orderTime = request.getOrderTime() != null ? request.getOrderTime() : LocalDateTime.now();

        int points = pointCalculator.calculateConsumptionPoints(
                member, request.getAmount(), category, orderTime, request.getActivityId());

        if (points <= 0) {
            log.info("积分计算为0，不入账: orderId={}, category={}", request.getOrderId(), category);
            throw new BusinessException("该品类不参与积分累计");
        }

        PointTransaction tx = PointTransaction.builder()
                .memberId(request.getMemberId())
                .points(points)
                .channel(PointChannel.CONSUMPTION)
                .description("消费返积分")
                .orderId(request.getOrderId())
                .activityId(request.getActivityId())
                .permanent(false)
                .createdAt(LocalDateTime.now())
                .build();

        memberService.addPoints(request.getMemberId(), points, false, PointChannel.CONSUMPTION);

        Member updated = memberService.getMember(request.getMemberId());
        tx.setBalanceAfter(updated.getAvailablePoints());

        transactionRepository.save(tx);
        log.info("消费积分入账: memberId={}, orderId={}, points={}",
                request.getMemberId(), request.getOrderId(), points);
        return tx;
    }

    public PointTransaction signIn(String memberId) {
        ReentrantLock lock = memberService.getMemberLock(memberId);
        lock.lock();
        try {
            Member member = memberService.getMember(memberId);
            LocalDate today = LocalDate.now(DEFAULT_ZONE);

            LocalDateTime lastSignIn = member.getLastSignInDate();
            if (lastSignIn != null && lastSignIn.toLocalDate().isEqual(today)) {
                throw new BusinessException("今日已签到");
            }

            int points = ruleService.getSignInRule().getDailyPoints();

            member.setLastSignInDate(LocalDateTime.now());
            member.setSignInDays(member.getSignInDays() + 1);

            memberService.addPoints(memberId, points, false, PointChannel.SIGN_IN);

            PointTransaction tx = PointTransaction.builder()
                    .memberId(memberId)
                    .points(points)
                    .channel(PointChannel.SIGN_IN)
                    .description("每日签到奖励")
                    .permanent(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            Member updated = memberService.getMember(memberId);
            tx.setBalanceAfter(updated.getAvailablePoints());

            transactionRepository.save(tx);
            log.info("签到奖励: memberId={}, points={}", memberId, points);
            return tx;
        } finally {
            lock.unlock();
        }
    }

    public PointTransaction completeProfile(String memberId) {
        ReentrantLock lock = memberService.getMemberLock(memberId);
        lock.lock();
        try {
            Member member = memberService.getMember(memberId);
            if (member.isProfileCompleted()) {
                throw new BusinessException("已领取过完善资料奖励");
            }

            member.setProfileCompleted(true);

            memberService.addPoints(memberId, PROFILE_COMPLETE_POINTS, false, PointChannel.PROFILE_COMPLETE);

            PointTransaction tx = PointTransaction.builder()
                    .memberId(memberId)
                    .points(PROFILE_COMPLETE_POINTS)
                    .channel(PointChannel.PROFILE_COMPLETE)
                    .description("完善个人资料奖励")
                    .permanent(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            Member updated = memberService.getMember(memberId);
            tx.setBalanceAfter(updated.getAvailablePoints());

            transactionRepository.save(tx);
            log.info("完善资料奖励: memberId={}, points={}", memberId, PROFILE_COMPLETE_POINTS);
            return tx;
        } finally {
            lock.unlock();
        }
    }

    public List<PointTransaction> handleReferral(String newMemberId) {
        Member preCheck = memberService.getMember(newMemberId);
        if (preCheck.getReferrerId() == null) {
            throw new BusinessException("无推荐人");
        }
        String referrerId = preCheck.getReferrerId();

        String firstId = newMemberId.compareTo(referrerId) < 0 ? newMemberId : referrerId;
        String secondId = newMemberId.compareTo(referrerId) < 0 ? referrerId : newMemberId;

        ReentrantLock firstLock = memberService.getMemberLock(firstId);
        ReentrantLock secondLock = memberService.getMemberLock(secondId);

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                Member newMember = memberService.getMember(newMemberId);
                if (newMember.isRefereeRewardGiven()) {
                    throw new BusinessException("推荐奖励已发放");
                }
                Member referrer = memberService.getMember(referrerId);
                if (referrer == null) {
                    throw new BusinessException("推荐人不存在: " + referrerId);
                }

                newMember.setRefereeRewardGiven(true);

                String referralId = "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);

                memberService.addPoints(referrerId, REFEREE_POINTS, true, PointChannel.REFEREE);
                memberService.addPoints(newMemberId, REFERRED_POINTS, true, PointChannel.REFERRED);

                PointTransaction referrerTx = PointTransaction.builder()
                        .memberId(referrerId)
                        .points(REFEREE_POINTS)
                        .channel(PointChannel.REFEREE)
                        .description("推荐新会员奖励")
                        .referralId(referralId)
                        .permanent(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                PointTransaction referredTx = PointTransaction.builder()
                        .memberId(newMemberId)
                        .points(REFERRED_POINTS)
                        .channel(PointChannel.REFERRED)
                        .description("被推荐新人奖励")
                        .referralId(referralId)
                        .permanent(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                Member r1 = memberService.getMember(referrerId);
                referrerTx.setBalanceAfter(r1.getAvailablePoints());
                Member r2 = memberService.getMember(newMemberId);
                referredTx.setBalanceAfter(r2.getAvailablePoints());

                transactionRepository.save(referrerTx);
                transactionRepository.save(referredTx);

                log.info("推荐奖励发放: referrerId={}, newMemberId={}, referralId={}", referrerId, newMemberId, referralId);
                return List.of(referrerTx, referredTx);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    public PointTransaction submitReview(String memberId, String orderId, String productId) {
        String reviewKey = orderId + "_" + productId;
        if (!reviewIndex.add(reviewKey)) {
            throw new BusinessException("该订单商品已评价过");
        }

        try {
            PointTransaction tx = PointTransaction.builder()
                    .memberId(memberId)
                    .points(REVIEW_POINTS)
                    .channel(PointChannel.REVIEW)
                    .description("商品评价奖励")
                    .orderId(orderId)
                    .reviewId("REV" + System.currentTimeMillis())
                    .permanent(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            memberService.addPoints(memberId, REVIEW_POINTS, false, PointChannel.REVIEW);

            Member updated = memberService.getMember(memberId);
            tx.setBalanceAfter(updated.getAvailablePoints());

            transactionRepository.save(tx);
            log.info("评价奖励: memberId={}, orderId={}, productId={}", memberId, orderId, productId);
            return tx;
        } catch (Exception e) {
            reviewIndex.remove(reviewKey);
            throw e;
        }
    }

    public PointTransaction addPoints(String memberId, int points, PointChannel channel,
                                      String description, boolean permanent, String relatedId) {
        PointTransaction tx = PointTransaction.builder()
                .memberId(memberId)
                .points(points)
                .channel(channel)
                .description(description)
                .permanent(permanent)
                .createdAt(LocalDateTime.now())
                .build();

        switch (channel) {
            case ACTIVITY -> tx.setActivityId(relatedId);
            case EXCHANGE_GOODS -> tx.setGoodsId(relatedId);
            case EXCHANGE_COUPON -> tx.setCouponId(relatedId);
            case LOTTERY -> tx.setLotteryId(relatedId);
            case DONATION -> tx.setDonationOrgId(relatedId);
            default -> {}
        }

        memberService.addPoints(memberId, points, permanent, channel);

        Member updated = memberService.getMember(memberId);
        tx.setBalanceAfter(updated.getAvailablePoints());

        transactionRepository.save(tx);
        return tx;
    }

    public PointTransaction subtractPoints(String memberId, int points, PointChannel channel,
                                           String description, String relatedId) {
        PointTransaction tx = PointTransaction.builder()
                .memberId(memberId)
                .points(-points)
                .channel(channel)
                .description(description)
                .permanent(false)
                .createdAt(LocalDateTime.now())
                .build();

        switch (channel) {
            case EXCHANGE_GOODS -> tx.setGoodsId(relatedId);
            case EXCHANGE_COUPON -> tx.setCouponId(relatedId);
            case LOTTERY -> tx.setLotteryId(relatedId);
            case DONATION -> tx.setDonationOrgId(relatedId);
            default -> {}
        }

        memberService.subtractPoints(memberId, points, channel);

        Member updated = memberService.getMember(memberId);
        tx.setBalanceAfter(updated.getAvailablePoints());

        transactionRepository.save(tx);
        return tx;
    }

    public List<PointTransaction> getTransactionsByMember(String memberId) {
        return transactionRepository.findByMemberId(memberId);
    }

    public PointTransaction getTransaction(Long id) {
        PointTransaction tx = transactionRepository.findById(id);
        if (tx == null) {
            throw new BusinessException("流水不存在: " + id);
        }
        return tx;
    }

    public List<PointTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
