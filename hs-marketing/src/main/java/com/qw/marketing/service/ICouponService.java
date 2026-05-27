package com.qw.marketing.service;

import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.marketing.service
 * @Project：home-serve
 * @name：ICouponService
 * @Date：2026/5/26
 * @Filename：ICouponService
 */
public interface ICouponService {
    List<CouponTemplates> getAvailableCoupons();

    void receiveCoupon(Long templateId, Long userId);

    List<UserCoupons> getMyCoupons(Long userId, Integer status);
}
