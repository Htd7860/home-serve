package com.qw.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户优惠券
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("user_coupons")
@Builder
public class UserCoupons implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券模板ID
     */
    private Long templateId;

    /**
     * 0未使用 1已使用 2已过期
     */
    private Integer status;

    /**
     * 使用的订单ID
     */
    private Long usedOrderId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    private LocalDateTime createdAt;


}
