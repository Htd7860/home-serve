package com.qw.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.admin.dto
 * @Project：home-serve
 * @name：PricingRuleRequest
 * @Date：2026/5/27
 * @Filename：PricingRuleRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRuleRequest {
    @NotEmpty
    String ruleType;
    @NotEmpty
    String ruleName;
    @NotEmpty
    String ruleConfig;
    @NotNull
    Integer priority;
    @NotNull
    Integer status;
}
