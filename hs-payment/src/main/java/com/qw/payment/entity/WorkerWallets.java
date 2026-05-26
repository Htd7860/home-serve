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
 * 服务者钱包
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("worker_wallets")
public class WorkerWallets implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务者ID
     */
    private Long workerId;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额（提现中）
     */
    private BigDecimal frozenBalance;

    /**
     * 累计收入
     */
    private BigDecimal totalEarned;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



}
