package com.loyalty.member.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.common.enums.MemberStatus;
import com.loyalty.member.dto.MemberCreateRequest;
import com.loyalty.member.dto.MemberUpdateRequest;
import com.loyalty.member.entity.Member;
import com.loyalty.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ApiResponse<Member> createMember(@Valid @RequestBody MemberCreateRequest request) {
        return ApiResponse.success(memberService.createMember(request));
    }

    @GetMapping("/{memberId}")
    public ApiResponse<Member> getMember(@PathVariable String memberId) {
        return ApiResponse.success(memberService.getMember(memberId));
    }

    @PutMapping("/{memberId}")
    public ApiResponse<Member> updateMember(@PathVariable String memberId,
                                            @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.success(memberService.updateMember(memberId, request));
    }

    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> deleteMember(@PathVariable String memberId) {
        memberService.deleteMember(memberId);
        return ApiResponse.success();
    }

    @PutMapping("/{memberId}/status")
    public ApiResponse<Member> updateStatus(@PathVariable String memberId,
                                            @RequestParam int status) {
        return ApiResponse.success(memberService.updateStatus(memberId, MemberStatus.fromCode(status)));
    }

    @GetMapping
    public ApiResponse<Collection<Member>> listMembers() {
        return ApiResponse.success(memberService.getAllMembers());
    }
}
