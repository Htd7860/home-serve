package com.qw.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 退款记录
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@TableName("refund_records")
public class RefundRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款流水号
     */
    private String refundNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金额
     */
    private BigDecimal amount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * PROCESSING / SUCCESS / FAILED
     */
    private Integer status;

    /**
     * 退款方式（原路返回）
     */
    private Integer refundMethod;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(Integer refundMethod) {
        this.refundMethod = refundMethod;
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
        return "RefundRecords{" +
        "id = " + id +
        ", refundNo = " + refundNo +
        ", orderId = " + orderId +
        ", userId = " + userId +
        ", amount = " + amount +
        ", reason = " + reason +
        ", status = " + status +
        ", refundMethod = " + refundMethod +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
