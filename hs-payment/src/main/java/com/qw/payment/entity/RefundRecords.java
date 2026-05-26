package com.qw.payment.entity;

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
 * 退款记录
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
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



}
