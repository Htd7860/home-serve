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
 * 收入流水
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
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


}
