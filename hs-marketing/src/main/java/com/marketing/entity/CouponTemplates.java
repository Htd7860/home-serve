package com.qw.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * FULL_REDUCTION / RATE
     */
    private String couponType;

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
     * 最高抵扣金额（折扣券）
     */
    private BigDecimal maxDiscount;

    /**
     * 总发行量
     */
    private Integer totalQuantity;

    /**
     * 领取后有效天数
     */
    private Integer validDays;

    /**
     * ON / OFF
     */
    private String status;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getValidDays() {
        return validDays;
    }

    public void setValidDays(Integer validDays) {
        this.validDays = validDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CouponTemplates{" +
        "id = " + id +
        ", couponName = " + couponName +
        ", couponType = " + couponType +
        ", thresholdAmount = " + thresholdAmount +
        ", discountAmount = " + discountAmount +
        ", discountRate = " + discountRate +
        ", maxDiscount = " + maxDiscount +
        ", totalQuantity = " + totalQuantity +
        ", validDays = " + validDays +
        ", status = " + status +
        ", createdAt = " + createdAt +
        "}";
    }
}
