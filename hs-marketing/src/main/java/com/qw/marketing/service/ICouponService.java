package com.qw.marketing.service;

import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.common.result.Result;

import java.util.List;

/**
 * <p>
 * 优惠券模板 服务类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
public interface ICouponService {
    List<UserCoupons> getMyCoupons(Integer status);

    List<CouponTemplates> getAvailableCoupons();

    Result receiveCoupons(Long templateId);
}
