package com.qw.common.constant;

/**
 * 秒杀活动状态
 */
public enum SeckillActivityStatus {
    DRAFT(0, "草稿"),
    PREHEAT(1, "预热"),
    IN_PROGRESS(2, "进行中"),
    ENDED(3, "已结束");

    private final int code;
    private final String desc;

    SeckillActivityStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static SeckillActivityStatus fromCode(int code) {
        for (SeckillActivityStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知活动状态: " + code);
    }
}
