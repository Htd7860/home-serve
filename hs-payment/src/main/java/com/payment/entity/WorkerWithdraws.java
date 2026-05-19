package com.qw.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 提现记录
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@TableName("worker_withdraws")
public class WorkerWithdraws implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提现流水号
     */
    private String withdrawNo;

    /**
     * 服务者ID
     */
    private Long workerId;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行卡号（生产环境需加密）
     */
    private String bankCardNo;

    /**
     * PROCESSING / SUCCESS / FAILED
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWithdrawNo() {
        return withdrawNo;
    }

    public void setWithdrawNo(String withdrawNo) {
        this.withdrawNo = withdrawNo;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
        return "WorkerWithdraws{" +
        "id = " + id +
        ", withdrawNo = " + withdrawNo +
        ", workerId = " + workerId +
        ", amount = " + amount +
        ", bankName = " + bankName +
        ", bankCardNo = " + bankCardNo +
        ", status = " + status +
        ", remark = " + remark +
        ", createdAt = " + createdAt +
        ", updatedAt = " + updatedAt +
        "}";
    }
}
