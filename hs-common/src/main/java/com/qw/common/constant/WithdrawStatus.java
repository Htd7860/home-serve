package com.qw.common.constant;

/**
 * 提现记录状态
 */
public enum WithdrawStatus {
    PROCESSING(0, "处理中"),
    SUCCESS(1, "提现成功"),
    FAILED(2, "提现失败");

    private final int code;
    private final String desc;

    WithdrawStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static WithdrawStatus fromCode(int code) {
        for (WithdrawStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知提现状态: " + code);
    }
}
