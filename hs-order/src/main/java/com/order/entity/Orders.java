package com.qw.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单主表
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * C端用户ID
     */
    private Long userId;

    /**
     * 接单服务者ID（抢单前为NULL）
     */
    private Long workerId;

    /**
     * 服务SKU ID
     */
    private Long skuId;

    /**
     * 服务品类
     */
    private Integer categoryId;

    /**
     * 服务地址ID
     */
    private Long addressId;

    /**
     * 预约时间
     */
    private LocalDateTime appointmentTime;

    /**
     * WAITING / GRABBED / SERVING / TO_CONFIRM / COMPLETED / CANCELLED / REFUNDING / REFUNDED
     */
    private String status;

    /**
     * 基础价
     */
    private BigDecimal basePrice;

    /**
     * 距离加价
     */
    private BigDecimal distanceFee;

    /**
     * 时段加价
     */
    private BigDecimal timeSurcharge;

    /**
     * 优惠券抵扣
     */
    private BigDecimal couponDiscount;

    /**
     * 实付金额
     */
    private BigDecimal finalPrice;

    /**
     * UNPAID / PAID / REFUNDING / REFUNDED
     */
    private String payStatus;

    /**
     * BALANCE / WECHAT / ALIPAY
     */
    private String payMethod;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * VIP订单走派单
     */
    private Byte isVip;

    /**
     * 紧急订单走派单
     */
    private Byte isUrgent;

    /**
     * 用户备注
     */
    private String userRemark;

    /**
     * 验收时间
     */
    private LocalDateTime confirmTime;

    /**
     * 是否24h自动验收
     */
    private Byte autoConfirm;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getDistanceFee() {
        return distanceFee;
    }

    public void setDistanceFee(BigDecimal distanceFee) {
        this.distanceFee = distanceFee;
    }

    public BigDecimal getTimeSurcharge() {
        return timeSurcharge;
    }

    public void setTimeSurcharge(BigDecimal timeSurcharge) {
        this.timeSurcharge = timeSurcharge;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public Byte getIsVip() {
        return isVip;
    }

    public void setIsVip(Byte isVip) {
        this.isVip = isVip;
    }

    public Byte getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Byte isUrgent) {
        this.isUrgent = isUrgent;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Byte getAutoConfirm() {
        return autoConfirm;
    }

    public void setAutoConfirm(Byte autoConfirm) {
        this.autoConfirm = autoConfirm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Orders{" +
        "id = " + id +
        ", orderNo = " + orderNo +
        ", userId = " + userId +
        ", workerId = " + workerId +
        ", skuId = " + skuId +
        ", categoryId = " + categoryId +
        ", addressId = " + addressId +
        ", appointmentTime = " + appointmentTime +
        ", status = " + status +
        ", basePrice = " + basePrice +
        ", distanceFee = " + distanceFee +
        ", timeSurcharge = " + timeSurcharge +
        ", couponDiscount = " + couponDiscount +
        ", finalPrice = " + finalPrice +
        ", payStatus = " + payStatus +
        ", payMethod = " + payMethod +
        ", payTime = " + payTime +
        ", isVip = " + isVip +
        ", isUrgent = " + isUrgent +
        ", userRemark = " + userRemark +
        ", confirmTime = " + confirmTime +
        ", autoConfirm = " + autoConfirm +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
