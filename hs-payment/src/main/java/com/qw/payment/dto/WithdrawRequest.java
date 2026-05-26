package com.qw.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author：qw
 * @Package：com.qw.payment.dto
 * @Project：home-serve
 * @name：WithdrawRequest
 * @Date：2026/5/25 20:09
 * @Filename：WithdrawRequest
 */
@Data
public class WithdrawRequest {
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String bankName;
    @NotBlank
    private String bankCardNo;
}
