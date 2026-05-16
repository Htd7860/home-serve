package com.qw.user.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.user.dto
 * @Project：home-serve
 * @name：LoginRequest
 * @Date：2026/5/16 10:07
 * @Filename：LoginRequest
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
   @Parameter(description = "1:普通用户 2:服务人员 3:管理员" )
   @Min(1)
   @Max(3)
    Integer loginType;
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式错误")
    String phone;
    @Size(groups =OnCodeLogin.class,min=6,max=6)
    String code;
    @Pattern(groups = OnPwdLogin.class,regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",message = "密码长度 8-32 位")
    String password;
}
