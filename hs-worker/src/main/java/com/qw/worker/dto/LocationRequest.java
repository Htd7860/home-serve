package com.qw.worker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author：qw
 * @Package：com.qw.worker.dto
 * @Project：home-serve
 * @name：LocationRequest
 * @Date：2026/5/20 19:31
 * @Filename：LocationRequest
 */
@Data
public class LocationRequest {
    @NotNull
    private BigDecimal lng;
    @NotNull
    private BigDecimal lat;
}
