package com.loyalty.expire.service;

import com.loyalty.common.enums.MemberStatus;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.expire.entity.ExpireRecord;
import com.loyalty.expire.repository.ExpireRecordRepository;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpireService {

    private final ExpireRecordRepository expireRecordRepository;
    private final MemberService memberService;
    private final PointTransactionRepository transactionRepository;

    @Scheduled(cron = "59 59 23 31 12 ?")
    public void scheduledYearEndExpire() {
        log.info("触发年度积分清零定时任务");
        int year = Year.now().getValue();
        executeExpire(year, "年度定时清零");
    }

    public ExpireRecord executeExpire(int year, String remark) {
        if (expireRecordRepository.existsByYear(year)) {
            throw new com.loyalty.common.BusinessException(year + " 年度清零已执行过");
        }

        String recordId = "EXP" + year + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        ExpireRecord record = ExpireRecord.builder()
                .recordId(recordId)
                .year(year)
                .affectedMembers(0)
                .totalExpiredPoints(0)
                .executedAt(LocalDateTime.now())
                .status("PROCESSING")
                .remark(remark)
                .build();
        expireRecordRepository.save(record);

        AtomicInteger affectedCount = new AtomicInteger(0);
        AtomicInteger totalExpired = new AtomicInteger(0);

        try {
            for (Member member : memberService.getAllMembers()) {
                if (member.getStatus() == MemberStatus.CANCELLING) {
                    continue;
                }
                int yearEarned = member.getYearEarnedPoints();
                if (yearEarned <= 0) {
                    continue;
                }

                int expireAmount = Math.min(yearEarned, member.getAvailablePoints() - member.getPermanentPoints());
                if (expireAmount <= 0) {
                    continue;
                }

                memberService.getMemberLock(member.getMemberId()).lock();
                try {
                    Member m = memberService.getMember(member.getMemberId());
                    int toExpire = Math.min(m.getYearEarnedPoints(), m.getAvailablePoints() - m.getPermanentPoints());
                    if (toExpire <= 0) {
                        continue;
                    }

                    m.setAvailablePoints(m.getAvailablePoints() - toExpire);
                    m.setYearEarnedPoints(m.getYearEarnedPoints() - toExpire);
                    m.setUpdatedAt(LocalDateTime.now());

                    PointTransaction tx = PointTransaction.builder()
                            .memberId(m.getMemberId())
                            .points(-toExpire)
                            .channel(PointChannel.EXPIRE)
                            .description(year + "年度积分清零")
                            .permanent(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    tx.setBalanceAfter(m.getAvailablePoints());
                    transactionRepository.save(tx);

                    affectedCount.incrementAndGet();
                    totalExpired.addAndGet(toExpire);
                } finally {
                    memberService.getMemberLock(member.getMemberId()).unlock();
                }
            }

            record.setAffectedMembers(affectedCount.get());
            record.setTotalExpiredPoints(totalExpired.get());
            record.setStatus("SUCCESS");
            expireRecordRepository.save(record);

            log.info("年度清零完成: year={}, affectedMembers={}, totalExpired={}",
                    year, affectedCount.get(), totalExpired.get());
        } catch (Exception e) {
            record.setStatus("FAILED");
            record.setRemark(e.getMessage());
            expireRecordRepository.save(record);
            log.error("年度清零失败", e);
            throw new com.loyalty.common.BusinessException("年度清零失败: " + e.getMessage());
        }

        return record;
    }

    public java.util.List<ExpireRecord> listRecords() {
        return expireRecordRepository.findAll();
    }

    public ExpireRecord getRecord(String recordId) {
        ExpireRecord record = expireRecordRepository.findById(recordId);
        if (record == null) {
            throw new com.loyalty.common.BusinessException("清零记录不存在: " + recordId);
        }
        return record;
    }
}
