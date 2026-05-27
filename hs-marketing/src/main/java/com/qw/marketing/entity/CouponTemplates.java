package com.qw.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 优惠券模板
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@TableName("coupon_templates")
public class CouponTemplates implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 券名：新人满100减20
     */
    private String couponName;

    /**
     * 1满减 2折扣
     */
    private Integer couponType;

    /**
     * 满减门槛（满减券）
     */
    private BigDecimal thresholdAmount;

    /**
     * 减扣金额（满减券）
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率（折扣券，如0.85）
     */
    private BigDecimal discountRate;

    /**
     * 总发行量
     */
    private Integer totalQuantity;

    /**
     * 领取后有效天数
     */
    private Integer validDays;

    /**
     * NULL通用券，有值绑定品类
     */
    private Integer categoryId;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 0普通券 1秒杀券
     */
    private Integer type;

    /**
     * 1启用 0禁用
     */
    private Integer status;

    private LocalDateTime createdAt;


}
