package com.qw.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.user.dto
 * @Project：home-serve
 * @name：RefreshRequest
 * @Date：2026/5/17 11:05
 * @Filename：RefreshRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    @NotBlank
    String refreshToken;
}
