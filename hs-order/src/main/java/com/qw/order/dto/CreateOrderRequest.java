package com.qw.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author：qw
 * @Package：com.qw.order.dto
 * @Project：home-serve
 * @name：CreateOrderRequest
 * @Date：2026/5/18 10:04
 * @Filename：CreateOrderRequest
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CreateOrderRequest {
    @NotNull
    Long skuId;
    @NotNull
    Long addressId;
    @NotNull
    LocalDateTime appointedTime;

    Long couponId;

    String userRemark;

    Integer isUrgent;
}
