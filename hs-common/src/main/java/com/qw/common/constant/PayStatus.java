package com.qw.common.constant;

/**
 * @Author：qw
 * @Package：com.qw.common.constant
 * @Project：home-serve
 * @name：PayStatus
 * @Date：2026/5/28 11:01
 * @Filename：PayStatus
 */
public enum PayStatus {
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUNDING(2, "退款中"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;
    PayStatus(int code,String desc){
        this.code=code;
        this.desc=desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static PayStatus fromCode(int code) {
        for (PayStatus s : values()) {
            if (s.code == code) {return s;}
        }
        throw new IllegalArgumentException("未知支付状态: " + code);
    }
}