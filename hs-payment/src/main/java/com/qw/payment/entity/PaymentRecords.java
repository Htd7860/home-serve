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
 * 支付流水
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("payment_records")
public class PaymentRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * BALANCE / WECHAT / ALIPAY
     */
    private Integer method;

    /**
     * PENDING / SUCCESS / FAILED
     */
    private Integer status;

    /**
     * 第三方支付流水号
     */
    private String thirdPartyNo;

    /**
     * 支付完成时间
     */
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

}
