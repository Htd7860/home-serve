package com.qw.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
     * 0待接单 1已接单 2服务中 3待验收 4已完成 5已取消 6退款中 7已退款
     */
    private Integer status;

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
     * 0未支付 1已支付 2退款中 3已退款
     */
    private Integer payStatus;

    /**
     * BALANCE / WECHAT / ALIPAY
     */
    private Integer payMethod;

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
    private Integer isUrgent;

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
    private BigDecimal urgentFee;

}
