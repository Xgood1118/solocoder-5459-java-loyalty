package com.loyalty.transfer.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.config.DataInitializer;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.repository.PointTransactionRepository;
import com.loyalty.transfer.dto.TransferRequest;
import com.loyalty.transfer.entity.TransferRecord;
import com.loyalty.transfer.repository.TransferRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRecordRepository transferRecordRepository;
    private final MemberService memberService;
    private final PointTransactionRepository transactionRepository;

    private static final double FEE_RATE = 0.1;
    private static final String SYSTEM_MEMBER_ID = DataInitializer.SYSTEM_MEMBER_ID;

    public TransferRecord transfer(TransferRequest request) {
        String fromId = request.getFromMemberId();
        String toId = request.getToMemberId();

        if (fromId.equals(toId)) {
            throw new BusinessException("不能给自己转账");
        }

        Member fromMember = memberService.getMember(fromId);
        Member toMember = memberService.getMember(toId);

        if (!fromMember.getStatus().canConsume()) {
            throw new BusinessException("转出方状态异常: " + fromMember.getStatus().getName());
        }
        if (!toMember.getStatus().canConsume()) {
            throw new BusinessException("转入方状态异常: " + toMember.getStatus().getName());
        }

        int amount = request.getAmount();
        int fee = (int) Math.floor(amount * FEE_RATE);
        int received = amount - fee;

        if (fromMember.getAvailablePoints() < amount) {
            throw new BusinessException("积分不足，可用积分: " + fromMember.getAvailablePoints());
        }

        List<String> ids = Arrays.asList(fromId, toId, SYSTEM_MEMBER_ID);
        ids.sort(Comparator.naturalOrder());

        ReentrantLock lock1 = memberService.getMemberLock(ids.get(0));
        ReentrantLock lock2 = memberService.getMemberLock(ids.get(1));
        ReentrantLock lock3 = memberService.getMemberLock(ids.get(2));

        lock1.lock();
        try {
            lock2.lock();
            try {
                lock3.lock();
                try {
                    return doTransfer(fromId, toId, amount, fee, received, request.getRemark());
                } finally {
                    lock3.unlock();
                }
            } finally {
                lock2.unlock();
            }
        } finally {
            lock1.unlock();
        }
    }

    private TransferRecord doTransfer(String fromId, String toId, int amount,
                                      int fee, int received, String remark) {
        String transferId = "TRF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        memberService.subtractPoints(fromId, amount, PointChannel.TRANSFER_OUT);

        boolean fromDeducted = true;
        boolean toAdded = false;
        boolean systemAdded = false;

        try {
            memberService.addPoints(toId, received, false, PointChannel.TRANSFER_IN);
            toAdded = true;

            memberService.addPoints(SYSTEM_MEMBER_ID, fee, true, PointChannel.TRANSFER_FEE);
            systemAdded = true;

            Member fromAfter = memberService.getMember(fromId);
            Member toAfter = memberService.getMember(toId);
            Member sysAfter = memberService.getMember(SYSTEM_MEMBER_ID);

            PointTransaction outTx = PointTransaction.builder()
                    .memberId(fromId)
                    .points(-amount)
                    .channel(PointChannel.TRANSFER_OUT)
                    .description("积分转出至 " + toId)
                    .referralId(transferId)
                    .permanent(false)
                    .balanceAfter(fromAfter.getAvailablePoints())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(outTx);

            PointTransaction inTx = PointTransaction.builder()
                    .memberId(toId)
                    .points(received)
                    .channel(PointChannel.TRANSFER_IN)
                    .description("积分转入自 " + fromId)
                    .referralId(transferId)
                    .permanent(false)
                    .balanceAfter(toAfter.getAvailablePoints())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(inTx);

            PointTransaction feeTx = PointTransaction.builder()
                    .memberId(SYSTEM_MEMBER_ID)
                    .points(fee)
                    .channel(PointChannel.TRANSFER_FEE)
                    .description("转账手续费收入")
                    .referralId(transferId)
                    .permanent(true)
                    .balanceAfter(sysAfter.getAvailablePoints())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(feeTx);

            TransferRecord record = TransferRecord.builder()
                    .transferId(transferId)
                    .fromMemberId(fromId)
                    .toMemberId(toId)
                    .amount(amount)
                    .fee(fee)
                    .receivedAmount(received)
                    .status("SUCCESS")
                    .remark(remark)
                    .createdAt(LocalDateTime.now())
                    .build();
            transferRecordRepository.save(record);

            log.info("转账成功: transferId={}, from={}, to={}, amount={}, fee={}",
                    transferId, fromId, toId, amount, fee);
            return record;
        } catch (Exception e) {
            log.error("转账过程异常，正在回滚: {}", e.getMessage());
            if (systemAdded) {
                memberService.subtractPoints(SYSTEM_MEMBER_ID, fee, PointChannel.TRANSFER_FEE);
            }
            if (toAdded) {
                memberService.subtractPoints(toId, received, PointChannel.TRANSFER_IN);
            }
            if (fromDeducted) {
                memberService.addPoints(fromId, amount, false, PointChannel.TRANSFER_OUT);
            }
            throw e;
        }
    }

    public List<TransferRecord> listTransferRecords(String memberId) {
        if (memberId != null) {
            return transferRecordRepository.findByMemberId(memberId);
        }
        return transferRecordRepository.findAll();
    }

    public TransferRecord getTransferRecord(String transferId) {
        TransferRecord record = transferRecordRepository.findById(transferId);
        if (record == null) {
            throw new BusinessException("转账记录不存在: " + transferId);
        }
        return record;
    }
}
