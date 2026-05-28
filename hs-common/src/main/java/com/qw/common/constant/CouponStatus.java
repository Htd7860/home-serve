package com.qw.common.constant;

/**
 * 用户优惠券状态
 */
public enum CouponStatus {
    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期");

    private final int code;
    private final String desc;

    CouponStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static CouponStatus fromCode(int code) {
        for (CouponStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知优惠券状态: " + code);
    }
}
