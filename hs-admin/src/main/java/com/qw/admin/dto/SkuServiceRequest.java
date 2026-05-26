package com.qw.admin.dto;

import com.qw.admin.validator.OnCreate;
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
 * @name：SkuServiceRequest
 * @Date：2026/5/26 20:39
 * @Filename：SkuServiceRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuServiceRequest {
    @NotNull(groups = OnCreate.class)
    Integer categoryId;
    @NotEmpty(groups = OnCreate.class)
    String name;
    @NotEmpty(groups = OnCreate.class)
    String description;
    @NotNull(groups = OnCreate.class)
    BigDecimal basePrice;
    @NotNull(groups = OnCreate.class)
    Integer durationMinutes;
    @NotEmpty(groups = OnCreate.class)
    String unit;
    @NotNull(groups = OnCreate.class)
    Integer status;
    String coverImage;
}
