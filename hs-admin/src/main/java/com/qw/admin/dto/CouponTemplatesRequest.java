package com.qw.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author：qw
 * @Package：com.qw.admin.dto
 * @Project：home-serve
 * @name：CouponTemplatesRequest
 * @Date：2026/5/27
 * @Filename：CouponTemplatesRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponTemplatesRequest {
    @NotEmpty
    String couponName;
    @NotNull
    Integer couponType;
    BigDecimal thresholdAmount;
    BigDecimal discountAmount;
    BigDecimal discountRate;
    @NotNull
    Integer validDays;
    Integer categoryId;
    @NotNull
    Integer type;
    @NotNull
    Integer status;
}
