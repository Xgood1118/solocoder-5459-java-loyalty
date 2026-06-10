package com.loyalty.rule.controller;

import com.loyalty.common.ApiResponse;
import com.loyalty.rule.entity.ActivityRule;
import com.loyalty.rule.entity.CategoryMultiplier;
import com.loyalty.rule.entity.SignInRule;
import com.loyalty.rule.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping("/categories")
    public ApiResponse<Collection<CategoryMultiplier>> listCategoryMultipliers() {
        return ApiResponse.success(ruleService.getAllCategoryMultipliers());
    }

    @GetMapping("/categories/{code}")
    public ApiResponse<CategoryMultiplier> getCategoryMultiplier(@PathVariable String code) {
        return ApiResponse.success(ruleService.getCategoryMultiplier(code));
    }

    @PutMapping("/categories/{code}")
    public ApiResponse<Void> updateCategoryMultiplier(@PathVariable String code,
                                                      @RequestParam double multiplier) {
        ruleService.updateCategoryMultiplier(code, multiplier);
        return ApiResponse.success();
    }

    @GetMapping("/sign-in")
    public ApiResponse<SignInRule> getSignInRule() {
        return ApiResponse.success(ruleService.getSignInRule());
    }

    @PutMapping("/sign-in")
    public ApiResponse<Void> updateSignInRule(@RequestBody SignInRule rule) {
        ruleService.updateSignInRule(rule);
        return ApiResponse.success();
    }

    @PostMapping("/activities")
    public ApiResponse<ActivityRule> createActivity(@RequestBody ActivityRule activity) {
        return ApiResponse.success(ruleService.createActivity(activity));
    }

    @GetMapping("/activities")
    public ApiResponse<Collection<ActivityRule>> listActivities() {
        return ApiResponse.success(ruleService.getAllActivities());
    }

    @GetMapping("/activities/{activityId}")
    public ApiResponse<ActivityRule> getActivity(@PathVariable String activityId) {
        return ApiResponse.success(ruleService.getActivity(activityId));
    }

    @PutMapping("/activities/{activityId}")
    public ApiResponse<Void> updateActivity(@PathVariable String activityId,
                                            @RequestBody ActivityRule activity) {
        ruleService.updateActivity(activityId, activity);
        return ApiResponse.success();
    }

    @DeleteMapping("/activities/{activityId}")
    public ApiResponse<Void> deleteActivity(@PathVariable String activityId) {
        ruleService.deleteActivity(activityId);
        return ApiResponse.success();
    }
}
