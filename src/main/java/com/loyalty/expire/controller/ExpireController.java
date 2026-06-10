package com.loyalty.expire.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.expire.entity.ExpireRecord;
import com.loyalty.expire.service.ExpireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expire")
@RequiredArgsConstructor
public class ExpireController {

    private final ExpireService expireService;

    @PostMapping("/execute")
    public ApiResponse<ExpireRecord> executeExpire(@RequestParam int year,
                                                   @RequestParam(required = false) String remark) {
        return ApiResponse.success(expireService.executeExpire(year, remark != null ? remark : "手动触发清零"));
    }

    @GetMapping("/records")
    public ApiResponse<List<ExpireRecord>> listRecords() {
        return ApiResponse.success(expireService.listRecords());
    }

    @GetMapping("/records/{recordId}")
    public ApiResponse<ExpireRecord> getRecord(@PathVariable String recordId) {
        return ApiResponse.success(expireService.getRecord(recordId));
    }
}
