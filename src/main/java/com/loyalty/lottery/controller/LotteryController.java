package com.loyalty.lottery.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.lottery.entity.LotteryRecord;
import com.loyalty.lottery.entity.Prize;
import com.loyalty.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    @PostMapping("/draw")
    public ApiResponse<LotteryRecord> draw(@RequestParam String memberId) {
        return ApiResponse.success(lotteryService.draw(memberId));
    }

    @GetMapping("/prizes")
    public ApiResponse<Collection<Prize>> listPrizes() {
        return ApiResponse.success(lotteryService.listPrizes());
    }

    @PostMapping("/prizes")
    public ApiResponse<Prize> addPrize(@RequestBody Prize prize) {
        return ApiResponse.success(lotteryService.addPrize(prize));
    }

    @PutMapping("/prizes/{prizeId}")
    public ApiResponse<Prize> updatePrize(@PathVariable String prizeId,
                                          @RequestBody Prize prize) {
        return ApiResponse.success(lotteryService.updatePrize(prizeId, prize));
    }

    @DeleteMapping("/prizes/{prizeId}")
    public ApiResponse<Void> deletePrize(@PathVariable String prizeId) {
        lotteryService.deletePrize(prizeId);
        return ApiResponse.success();
    }

    @GetMapping("/records")
    public ApiResponse<List<LotteryRecord>> listRecords(
            @RequestParam(required = false) String memberId) {
        return ApiResponse.success(lotteryService.listRecords(memberId));
    }

    @GetMapping("/records/{recordId}")
    public ApiResponse<LotteryRecord> getRecord(@PathVariable String recordId) {
        return ApiResponse.success(lotteryService.getRecord(recordId));
    }
}
