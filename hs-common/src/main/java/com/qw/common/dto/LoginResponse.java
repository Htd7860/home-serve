package com.qw.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.user.dto
 * @Project：home-serve
 * @name：LoginResponse
 * @Date：2026/5/16 10:13
 * @Filename：LoginResponse
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    String token;
    String refreshToken;
    Long userId;
    Integer loginType;
    String avatarUrl;
    String nickname;
}
