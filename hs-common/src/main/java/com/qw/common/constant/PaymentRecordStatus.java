package com.qw.common.constant;

/**
 * 支付流水状态
 */
public enum PaymentRecordStatus {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败");

    private final int code;
    private final String desc;

    PaymentRecordStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static PaymentRecordStatus fromCode(int code) {
        for (PaymentRecordStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知支付流水状态: " + code);
    }
}
