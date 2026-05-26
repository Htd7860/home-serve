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
 * 提现记录
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
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

}
