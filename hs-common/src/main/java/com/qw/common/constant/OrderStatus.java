package com.qw.common.constant;

/**
 * @Author：qw
 * @Package：com.qw.common.constant
 * @Project：home-serve
 * @name：OrderStatus
 * @Date：2026/5/28 11:00
 * @Filename：OrderStatus
 */
public enum OrderStatus {

    WAITING(0, "待接单"),
    GRABBED(1, "已接单"),
    SERVING(2, "服务中"),
    TO_CONFIRM(3, "待验收"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消"),
    REFUNDING(6, "退款中"),
    REFUNDED(7, "已退款");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) {return s;}
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}