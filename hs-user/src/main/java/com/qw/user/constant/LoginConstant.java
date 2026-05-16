package com.qw.user.constant;

/**
 * @Author：qw
 * @Package：com.qw.user.constant
 * @Project：home-serve
 * @name：LoginConstant
 * @Date：2026/5/16 11:55
 * @Filename：LoginConstant
 */
public class LoginConstant {
    public static final  String PHONE_FORMAT_ERROR="手机号格式错误";
    public static final String PHONE_RETRY_ERROR="请求过于频繁，请稍后再试！";
    public static final int LOGIN_TYPE_USER=1;
    public static final int LOGIN_TYPE_WORKER=2;
    public static final int LOGIN_TYPE_ADMIN=3;
    public static final String LOGIN_PASSWORD_ERROR="密码错误!";
    public static final String PHONE_NOT_EXISTS="该手机号尚未注册";
    public static final String PHONE_ALREADY_EXISTS="该手机已经注册";
    public static final String ADMIN_PHONE_ERROR="非法用户";
    public static final String LOGIN_USER_ERROR="验证失败";
    public static final String PHONE_FORMAT="^1[3-9]\\d{9}$";
    public static final Long FIXED_JWT_TIME=1000L*3600*2;
    public static final Long REFRESH_JWT_TIME=1000L*3600*24*7;
    public static final String  LOGIN_CODE_ERROR="验证码错误";

}
