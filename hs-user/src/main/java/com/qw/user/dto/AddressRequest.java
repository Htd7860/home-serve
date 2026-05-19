package com.qw.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author：qw
 * @Package：com.qw.user.dto
 * @Project：home-serve
 * @name：AddressRequest
 * @Date：2026/5/18 19:53
 * @Filename：AddressRequest
 */
@Data
public class AddressRequest {

    @NotBlank
    private String contactName;

    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String contactPhone;

    @NotBlank
    private String province;

    @NotBlank
    private String city;

    @NotBlank
    private String district;

    @NotBlank
    private String detail;

    private Byte isDefault;

    @NotNull
    private BigDecimal lng;

    @NotNull
    private BigDecimal lat;
}