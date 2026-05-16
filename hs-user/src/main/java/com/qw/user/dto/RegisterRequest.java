package com.qw.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：qw
 * @Package：com.qw.user.dto
 * @Project：home-serve
 * @name：RegisterRequest
 * @Date：2026/5/16 15:16
 * @Filename：RegisterRequest
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Pattern(regexp = "\\w[1-10]",message = "昵称应由1-10个数字、字母、下划线组成")
    String nickName;
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式错误")
    String phone;
    @Size(groups =OnCodeRegister.class,min=6,max=6)
    String code;
    @Min(1)
    @Max(3)
    int registerType;
    @NotBlank(groups = OnPasswordRegister.class)
    @Size(groups = OnPasswordRegister.class,min = 8, max = 32, message = "密码长度 8-32 位")
    @Pattern(groups = OnPasswordRegister.class,regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码需包含大小写字母和数字")
    String password;
    @NotBlank(groups = OnPasswordRegister.class)
    @Size(groups = OnPasswordRegister.class,min = 8, max = 32, message = "密码长度 8-32 位")
    @Pattern(groups = OnPasswordRegister.class,regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码需包含大小写字母和数字")
    String rePassword;
}
