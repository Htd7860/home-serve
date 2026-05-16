package com.qw.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 收入流水
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@TableName("worker_earnings")
public class WorkerEarnings implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务者ID
     */
    private Long workerId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单金额
     */
    private BigDecimal orderPrice;

    /**
     * 分账比例（如0.80）
     */
    private BigDecimal workerRatio;

    /**
     * 服务者所得
     */
    private BigDecimal workerAmount;

    /**
     * 平台抽成
     */
    private BigDecimal platformAmount;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getWorkerRatio() {
        return workerRatio;
    }

    public void setWorkerRatio(BigDecimal workerRatio) {
        this.workerRatio = workerRatio;
    }

    public BigDecimal getWorkerAmount() {
        return workerAmount;
    }

    public void setWorkerAmount(BigDecimal workerAmount) {
        this.workerAmount = workerAmount;
    }

    public BigDecimal getPlatformAmount() {
        return platformAmount;
    }

    public void setPlatformAmount(BigDecimal platformAmount) {
        this.platformAmount = platformAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WorkerEarnings{" +
        "id = " + id +
        ", workerId = " + workerId +
        ", orderId = " + orderId +
        ", orderPrice = " + orderPrice +
        ", workerRatio = " + workerRatio +
        ", workerAmount = " + workerAmount +
        ", platformAmount = " + platformAmount +
        ", createdAt = " + createdAt +
        "}";
    }
}
