package com.qw.order.dto;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    Long skuId;
    @NotBlank
    Long addressId;
    @NotBlank
    LocalDateTime localDateTime;

    Long couponId;

    String userRemark;

    Byte isUrgent;
}
