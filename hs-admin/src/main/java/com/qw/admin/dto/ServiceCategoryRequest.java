package com.qw.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.admin.dto
 * @Project：home-serve
 * @name：ServiceCategoryRequest
 * @Date：2026/5/26 18:54
 * @Filename：ServiceCategoryRequest
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryRequest {
    @NotBlank
    String name;
    Integer sortOrder;
    String iconUrl;
}
