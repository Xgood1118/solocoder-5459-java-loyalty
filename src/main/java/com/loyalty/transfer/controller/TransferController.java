package com.loyalty.transfer.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.transfer.dto.TransferRequest;
import com.loyalty.transfer.entity.TransferRecord;
import com.loyalty.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ApiResponse<TransferRecord> transfer(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.success(transferService.transfer(request));
    }

    @GetMapping
    public ApiResponse<List<TransferRecord>> listTransfers(
            @RequestParam(required = false) String memberId) {
        return ApiResponse.success(transferService.listTransferRecords(memberId));
    }

    @GetMapping("/{transferId}")
    public ApiResponse<TransferRecord> getTransfer(@PathVariable String transferId) {
        return ApiResponse.success(transferService.getTransferRecord(transferId));
    }
}
