package com.loyalty.member.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.MemberLevel;
import com.loyalty.common.enums.MemberStatus;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.member.dto.MemberCreateRequest;
import com.loyalty.member.dto.MemberUpdateRequest;
import com.loyalty.member.entity.Member;
import com.loyalty.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public Member createMember(MemberCreateRequest request) {
        String memberId = "M" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        LocalDateTime now = LocalDateTime.now();
        Member member = Member.builder()
                .memberId(memberId)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .birthday(request.getBirthday())
                .level(MemberLevel.NORMAL)
                .status(MemberStatus.ACTIVE)
                .totalPoints(0)
                .availablePoints(0)
                .frozenPoints(0)
                .permanentPoints(0)
                .yearEarnedPoints(0)
                .profileCompleted(false)
                .referrerId(request.getReferrerId())
                .refereeRewardGiven(false)
                .signInDays(0)
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .build();

        memberRepository.save(member);
        log.info("创建会员成功: memberId={}, name={}", memberId, request.getName());
        return member;
    }

    public Member getMember(String memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在: " + memberId);
        }
        return member;
    }

    public Member updateMember(String memberId, MemberUpdateRequest request) {
        Member member = getMember(memberId);
        if (request.getName() != null) {
            member.setName(request.getName());
        }
        if (request.getPhone() != null) {
            member.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            member.setEmail(request.getEmail());
        }
        if (request.getBirthday() != null) {
            member.setBirthday(request.getBirthday());
        }
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);
        log.info("更新会员信息: memberId={}", memberId);
        return member;
    }

    public void deleteMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException("会员不存在: " + memberId);
        }
        memberRepository.deleteById(memberId);
        log.info("删除会员: memberId={}", memberId);
    }

    public Member updateStatus(String memberId, MemberStatus status) {
        Member member = getMember(memberId);
        member.setStatus(status);
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);
        log.info("更新会员状态: memberId={}, status={}", memberId, status.getName());
        return member;
    }

    public ReentrantLock getMemberLock(String memberId) {
        return lockMap.computeIfAbsent(memberId, k -> new ReentrantLock());
    }

    public void addPoints(String memberId, int points, boolean permanent, PointChannel channel) {
        ReentrantLock lock = getMemberLock(memberId);
        lock.lock();
        try {
            Member member = getMember(memberId);
            member.setTotalPoints(member.getTotalPoints() + points);
            member.setAvailablePoints(member.getAvailablePoints() + points);
            if (permanent) {
                member.setPermanentPoints(member.getPermanentPoints() + points);
            } else {
                int currentYear = Year.now().getValue();
                member.setYearEarnedPoints(member.getYearEarnedPoints() + points);
            }
            MemberLevel oldLevel = member.getLevel();
            MemberLevel newLevel = MemberLevel.fromPoints(member.getTotalPoints());
            if (oldLevel != newLevel) {
                member.setLevel(newLevel);
                log.info("会员等级变更: memberId={}, {} -> {}", memberId, oldLevel.getName(), newLevel.getName());
            }
            member.setUpdatedAt(LocalDateTime.now());
            member.setVersion(member.getVersion() + 1);
            memberRepository.save(member);
        } finally {
            lock.unlock();
        }
    }

    public void subtractPoints(String memberId, int points, PointChannel channel) {
        ReentrantLock lock = getMemberLock(memberId);
        lock.lock();
        try {
            Member member = getMember(memberId);
            if (!member.getStatus().canConsume()) {
                throw new BusinessException("会员状态异常，无法扣减积分: " + member.getStatus().getName());
            }
            if (member.getAvailablePoints() < points) {
                throw new BusinessException("积分不足，可用积分: " + member.getAvailablePoints());
            }
            member.setAvailablePoints(member.getAvailablePoints() - points);

            int nonPermanentToDeduct = Math.min(points, member.getYearEarnedPoints());
            int permanentToDeduct = points - nonPermanentToDeduct;
            member.setYearEarnedPoints(member.getYearEarnedPoints() - nonPermanentToDeduct);
            if (permanentToDeduct > 0) {
                member.setPermanentPoints(member.getPermanentPoints() - permanentToDeduct);
            }

            member.setUpdatedAt(LocalDateTime.now());
            member.setVersion(member.getVersion() + 1);
            memberRepository.save(member);
        } finally {
            lock.unlock();
        }
    }

    public Collection<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public int getMemberCount() {
        return memberRepository.count();
    }
}
