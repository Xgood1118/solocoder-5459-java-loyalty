package com.loyalty.level.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.common.enums.MemberLevel;
import com.loyalty.level.entity.LevelBenefit;
import com.loyalty.level.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;

    @GetMapping("/benefits")
    public ApiResponse<Map<MemberLevel, LevelBenefit>> listBenefits() {
        return ApiResponse.success(levelService.getAllBenefits());
    }

    @GetMapping("/benefits/{level}")
    public ApiResponse<LevelBenefit> getBenefit(@PathVariable int level) {
        return ApiResponse.success(levelService.getBenefit(MemberLevel.fromCode(level)));
    }

    @GetMapping("/thresholds")
    public ApiResponse<Map<MemberLevel, Integer>> listThresholds() {
        return ApiResponse.success(levelService.getAllThresholds());
    }

    @PutMapping("/thresholds/{level}")
    public ApiResponse<Void> updateThreshold(@PathVariable int level,
                                             @RequestParam int threshold) {
        levelService.updateThreshold(MemberLevel.fromCode(level), threshold);
        return ApiResponse.success();
    }

    @PutMapping("/benefits/{level}")
    public ApiResponse<Void> updateBenefit(@PathVariable int level,
                                           @RequestBody LevelBenefit benefit) {
        levelService.updateBenefit(MemberLevel.fromCode(level), benefit);
        return ApiResponse.success();
    }
}
