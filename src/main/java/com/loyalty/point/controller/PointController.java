package com.loyalty.point.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.point.dto.ConsumptionPointRequest;
import com.loyalty.point.dto.SignInRequest;
import com.loyalty.point.entity.PointTransaction;
import com.loyalty.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn/consumption")
    public ApiResponse<PointTransaction> earnByConsumption(@Valid @RequestBody ConsumptionPointRequest request) {
        return ApiResponse.success(pointService.earnByConsumption(request));
    }

    @PostMapping("/earn/sign-in")
    public ApiResponse<PointTransaction> signIn(@Valid @RequestBody SignInRequest request) {
        return ApiResponse.success(pointService.signIn(request.getMemberId()));
    }

    @PostMapping("/earn/profile-complete/{memberId}")
    public ApiResponse<PointTransaction> completeProfile(@PathVariable String memberId) {
        return ApiResponse.success(pointService.completeProfile(memberId));
    }

    @PostMapping("/earn/referral/{newMemberId}")
    public ApiResponse<List<PointTransaction>> handleReferral(@PathVariable String newMemberId) {
        return ApiResponse.success(pointService.handleReferral(newMemberId));
    }

    @PostMapping("/earn/review")
    public ApiResponse<PointTransaction> submitReview(@RequestParam String memberId,
                                                      @RequestParam String orderId,
                                                      @RequestParam String productId) {
        return ApiResponse.success(pointService.submitReview(memberId, orderId, productId));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<PointTransaction>> listTransactions(
            @RequestParam(required = false) String memberId) {
        if (memberId != null) {
            return ApiResponse.success(pointService.getTransactionsByMember(memberId));
        }
        return ApiResponse.success(pointService.getAllTransactions());
    }

    @GetMapping("/transactions/{id}")
    public ApiResponse<PointTransaction> getTransaction(@PathVariable Long id) {
        return ApiResponse.success(pointService.getTransaction(id));
    }
}
