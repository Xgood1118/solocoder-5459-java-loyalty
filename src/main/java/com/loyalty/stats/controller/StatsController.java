package com.loyalty.stats.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.stats.dto.LevelDistribution;
import com.loyalty.stats.dto.PointStats;
import com.loyalty.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        return ApiResponse.success(statsService.getOverview());
    }

    @GetMapping("/points")
    public ApiResponse<PointStats> getPointStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ApiResponse.success(statsService.getPointStats(startTime, endTime));
    }

    @GetMapping("/levels")
    public ApiResponse<List<LevelDistribution>> getLevelDistribution() {
        return ApiResponse.success(statsService.getLevelDistribution());
    }
}
