package com.loyalty.redemption.service;

import com.loyalty.common.BusinessException;
import com.loyalty.common.enums.PointChannel;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.repository.PointTransactionRepository;
import com.loyalty.redemption.entity.*;
import com.loyalty.redemption.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final ExchangeGoodsRepository goodsRepository;
    private final CouponRepository couponRepository;
    private final CouponRecordRepository couponRecordRepository;
    private final DonationOrgRepository donationOrgRepository;
    private final MemberService memberService;
    private final PointTransactionRepository transactionRepository;

    @PostConstruct
    public void initDefaults() {
        if (goodsRepository.findAll().isEmpty()) {
            goodsRepository.save(ExchangeGoods.builder()
                    .goodsId("GOODS_001")
                    .name("精美水杯")
                    .price(50)
                    .pointsRequired(5000)
                    .stock(100)
                    .description("高品质不锈钢保温杯")
                    .enabled(true)
                    .build());
            goodsRepository.save(ExchangeGoods.builder()
                    .goodsId("GOODS_002")
                    .name("购物袋")
                    .price(20)
                    .pointsRequired(2000)
                    .stock(500)
                    .description("环保购物袋")
                    .enabled(true)
                    .build());
        }

        if (couponRepository.findAll().isEmpty()) {
            couponRepository.save(Coupon.builder()
                    .couponTemplateId("COUPON_50")
                    .name("50元优惠券")
                    .faceValue(50)
                    .pointsRequired(500)
                    .totalCount(1000)
                    .issuedCount(0)
                    .validDays(30)
                    .enabled(true)
                    .build());
            couponRepository.save(Coupon.builder()
                    .couponTemplateId("COUPON_100")
                    .name("100元优惠券")
                    .faceValue(100)
                    .pointsRequired(1000)
                    .totalCount(500)
                    .issuedCount(0)
                    .validDays(30)
                    .enabled(true)
                    .build());
        }

        if (donationOrgRepository.findAll().isEmpty()) {
            donationOrgRepository.save(DonationOrg.builder()
                    .orgId("ORG_001")
                    .name("希望工程基金会")
                    .description("支持贫困地区儿童教育")
                    .totalDonatedPoints(0)
                    .enabled(true)
                    .build());
            donationOrgRepository.save(DonationOrg.builder()
                    .orgId("ORG_002")
                    .name("绿色环保协会")
                    .description("支持环保公益事业")
                    .totalDonatedPoints(0)
                    .enabled(true)
                    .build());
        }
    }

    public Collection<ExchangeGoods> listGoods() {
        return goodsRepository.findAll();
    }

    public ExchangeGoods addGoods(ExchangeGoods goods) {
        if (goods.getGoodsId() == null) {
            goods.setGoodsId("GOODS" + System.currentTimeMillis());
        }
        goods.setEnabled(true);
        return goodsRepository.save(goods);
    }

    public ExchangeGoods updateGoods(String goodsId, ExchangeGoods goods) {
        if (goodsRepository.findById(goodsId) == null) {
            throw new BusinessException("商品不存在: " + goodsId);
        }
        goods.setGoodsId(goodsId);
        return goodsRepository.save(goods);
    }

    public void deleteGoods(String goodsId) {
        if (goodsRepository.findById(goodsId) == null) {
            throw new BusinessException("商品不存在: " + goodsId);
        }
        goodsRepository.deleteById(goodsId);
    }

    public PointTransaction exchangeGoods(String memberId, String goodsId, int quantity) {
        ExchangeGoods goods = goodsRepository.findById(goodsId);
        if (goods == null || !goods.isEnabled()) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (quantity <= 0) {
            throw new BusinessException("兑换数量必须大于0");
        }
        if (goods.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }

        int totalPoints = goods.getPointsRequired() * quantity;

        Member member = memberService.getMember(memberId);
        if (member.getAvailablePoints() < totalPoints) {
            throw new BusinessException("积分不足，需要 " + totalPoints + " 积分");
        }

        memberService.getMemberLock(memberId).lock();
        try {
            goods.setStock(goods.getStock() - quantity);
            goodsRepository.save(goods);

            memberService.subtractPoints(memberId, totalPoints, PointChannel.EXCHANGE_GOODS);

            Member after = memberService.getMember(memberId);
            PointTransaction tx = PointTransaction.builder()
                    .memberId(memberId)
                    .points(-totalPoints)
                    .channel(PointChannel.EXCHANGE_GOODS)
                    .description("兑换商品: " + goods.getName() + " x" + quantity)
                    .goodsId(goodsId)
                    .permanent(false)
                    .balanceAfter(after.getAvailablePoints())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);

            log.info("商品兑换成功: memberId={}, goodsId={}, quantity={}, points={}",
                    memberId, goodsId, quantity, totalPoints);
            return tx;
        } catch (Exception e) {
            goods.setStock(goods.getStock() + quantity);
            goodsRepository.save(goods);
            throw e;
        } finally {
            memberService.getMemberLock(memberId).unlock();
        }
    }

    public Collection<Coupon> listCoupons() {
        return couponRepository.findAll();
    }

    public Coupon addCoupon(Coupon coupon) {
        if (coupon.getCouponTemplateId() == null) {
            coupon.setCouponTemplateId("COUPON" + System.currentTimeMillis());
        }
        return couponRepository.save(coupon);
    }

    public Coupon updateCoupon(String templateId, Coupon coupon) {
        if (couponRepository.findById(templateId) == null) {
            throw new BusinessException("优惠券模板不存在: " + templateId);
        }
        coupon.setCouponTemplateId(templateId);
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(String templateId) {
        if (couponRepository.findById(templateId) == null) {
            throw new BusinessException("优惠券模板不存在: " + templateId);
        }
        couponRepository.deleteById(templateId);
    }

    public CouponRecord exchangeCoupon(String memberId, String templateId) {
        Coupon template = couponRepository.findById(templateId);
        if (template == null || !template.isEnabled()) {
            throw new BusinessException("优惠券不存在或已下架");
        }

        if (template.getTotalCount() > 0 && template.getIssuedCount() >= template.getTotalCount()) {
            throw new BusinessException("优惠券已领完");
        }

        Member member = memberService.getMember(memberId);
        if (member.getAvailablePoints() < template.getPointsRequired()) {
            throw new BusinessException("积分不足，需要 " + template.getPointsRequired() + " 积分");
        }

        memberService.getMemberLock(memberId).lock();
        try {
            template.setIssuedCount(template.getIssuedCount() + 1);
            couponRepository.save(template);

            memberService.subtractPoints(memberId, template.getPointsRequired(), PointChannel.EXCHANGE_COUPON);

            String couponId = "C" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            LocalDateTime now = LocalDateTime.now();
            CouponRecord record = CouponRecord.builder()
                    .couponId(couponId)
                    .templateId(templateId)
                    .memberId(memberId)
                    .name(template.getName())
                    .faceValue(template.getFaceValue())
                    .status("ISSUED")
                    .issuedAt(now)
                    .expireAt(template.getValidDays() > 0 ? now.plusDays(template.getValidDays()) : null)
                    .build();
            couponRecordRepository.save(record);

            Member after = memberService.getMember(memberId);
            PointTransaction tx = PointTransaction.builder()
                    .memberId(memberId)
                    .points(-template.getPointsRequired())
                    .channel(PointChannel.EXCHANGE_COUPON)
                    .description("兑换优惠券: " + template.getName())
                    .couponId(couponId)
                    .permanent(false)
                    .balanceAfter(after.getAvailablePoints())
                    .createdAt(now)
                    .build();
            transactionRepository.save(tx);

            log.info("优惠券兑换成功: memberId={}, templateId={}, couponId={}",
                    memberId, templateId, couponId);
            return record;
        } catch (Exception e) {
            template.setIssuedCount(template.getIssuedCount() - 1);
            couponRepository.save(template);
            throw e;
        } finally {
            memberService.getMemberLock(memberId).unlock();
        }
    }

    public List<CouponRecord> listMyCoupons(String memberId) {
        return couponRecordRepository.findByMemberId(memberId);
    }

    public Collection<DonationOrg> listDonationOrgs() {
        return donationOrgRepository.findAll();
    }

    public DonationOrg addDonationOrg(DonationOrg org) {
        if (org.getOrgId() == null) {
            org.setOrgId("ORG" + System.currentTimeMillis());
        }
        org.setEnabled(true);
        return donationOrgRepository.save(org);
    }

    public void deleteDonationOrg(String orgId) {
        if (donationOrgRepository.findById(orgId) == null) {
            throw new BusinessException("机构不存在: " + orgId);
        }
        donationOrgRepository.deleteById(orgId);
    }

    public PointTransaction donate(String memberId, String orgId, int points) {
        DonationOrg org = donationOrgRepository.findById(orgId);
        if (org == null || !org.isEnabled()) {
            throw new BusinessException("捐赠机构不存在或已停用");
        }
        if (points <= 0) {
            throw new BusinessException("捐赠积分必须大于0");
        }

        Member member = memberService.getMember(memberId);
        if (member.getAvailablePoints() < points) {
            throw new BusinessException("积分不足");
        }

        memberService.getMemberLock(memberId).lock();
        try {
            memberService.subtractPoints(memberId, points, PointChannel.DONATION);

            org.setTotalDonatedPoints(org.getTotalDonatedPoints() + points);
            donationOrgRepository.save(org);

            Member after = memberService.getMember(memberId);
            PointTransaction tx = PointTransaction.builder()
                    .memberId(memberId)
                    .points(-points)
                    .channel(PointChannel.DONATION)
                    .description("捐赠至: " + org.getName())
                    .donationOrgId(orgId)
                    .permanent(false)
                    .balanceAfter(after.getAvailablePoints())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);

            log.info("公益捐赠: memberId={}, orgId={}, points={}", memberId, orgId, points);
            return tx;
        } finally {
            memberService.getMemberLock(memberId).unlock();
        }
    }
}
