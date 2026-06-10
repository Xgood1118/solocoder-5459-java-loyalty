package com.loyalty.redemption.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.redemption.entity.*;
import com.loyalty.redemption.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/redemption")
@RequiredArgsConstructor
public class RedemptionController {

    private final RedemptionService redemptionService;

    @GetMapping("/goods")
    public ApiResponse<Collection<ExchangeGoods>> listGoods() {
        return ApiResponse.success(redemptionService.listGoods());
    }

    @PostMapping("/goods")
    public ApiResponse<ExchangeGoods> addGoods(@RequestBody ExchangeGoods goods) {
        return ApiResponse.success(redemptionService.addGoods(goods));
    }

    @PutMapping("/goods/{goodsId}")
    public ApiResponse<ExchangeGoods> updateGoods(@PathVariable String goodsId,
                                                  @RequestBody ExchangeGoods goods) {
        return ApiResponse.success(redemptionService.updateGoods(goodsId, goods));
    }

    @DeleteMapping("/goods/{goodsId}")
    public ApiResponse<Void> deleteGoods(@PathVariable String goodsId) {
        redemptionService.deleteGoods(goodsId);
        return ApiResponse.success();
    }

    @PostMapping("/goods/exchange")
    public ApiResponse<PointTransaction> exchangeGoods(@RequestParam String memberId,
                                                       @RequestParam String goodsId,
                                                       @RequestParam(defaultValue = "1") int quantity) {
        return ApiResponse.success(redemptionService.exchangeGoods(memberId, goodsId, quantity));
    }

    @GetMapping("/coupons")
    public ApiResponse<Collection<Coupon>> listCoupons() {
        return ApiResponse.success(redemptionService.listCoupons());
    }

    @PostMapping("/coupons")
    public ApiResponse<Coupon> addCoupon(@RequestBody Coupon coupon) {
        return ApiResponse.success(redemptionService.addCoupon(coupon));
    }

    @PutMapping("/coupons/{templateId}")
    public ApiResponse<Coupon> updateCoupon(@PathVariable String templateId,
                                            @RequestBody Coupon coupon) {
        return ApiResponse.success(redemptionService.updateCoupon(templateId, coupon));
    }

    @DeleteMapping("/coupons/{templateId}")
    public ApiResponse<Void> deleteCoupon(@PathVariable String templateId) {
        redemptionService.deleteCoupon(templateId);
        return ApiResponse.success();
    }

    @PostMapping("/coupons/exchange")
    public ApiResponse<CouponRecord> exchangeCoupon(@RequestParam String memberId,
                                                    @RequestParam String templateId) {
        return ApiResponse.success(redemptionService.exchangeCoupon(memberId, templateId));
    }

    @GetMapping("/coupons/my")
    public ApiResponse<List<CouponRecord>> listMyCoupons(@RequestParam String memberId) {
        return ApiResponse.success(redemptionService.listMyCoupons(memberId));
    }

    @GetMapping("/donation/orgs")
    public ApiResponse<Collection<DonationOrg>> listDonationOrgs() {
        return ApiResponse.success(redemptionService.listDonationOrgs());
    }

    @PostMapping("/donation/orgs")
    public ApiResponse<DonationOrg> addDonationOrg(@RequestBody DonationOrg org) {
        return ApiResponse.success(redemptionService.addDonationOrg(org));
    }

    @DeleteMapping("/donation/orgs/{orgId}")
    public ApiResponse<Void> deleteDonationOrg(@PathVariable String orgId) {
        redemptionService.deleteDonationOrg(orgId);
        return ApiResponse.success();
    }

    @PostMapping("/donation")
    public ApiResponse<PointTransaction> donate(@RequestParam String memberId,
                                                @RequestParam String orgId,
                                                @RequestParam int points) {
        return ApiResponse.success(redemptionService.donate(memberId, orgId, points));
    }
}
