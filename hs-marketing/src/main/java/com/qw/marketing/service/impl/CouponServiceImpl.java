package com.qw.marketing.service.impl;

import com.qw.common.exception.BizException;
import com.qw.marketing.entity.CouponTemplates;
import com.qw.marketing.entity.UserCoupons;
import com.qw.marketing.mapper.CouponsMapper;
import com.qw.marketing.service.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.marketing.service.impl
 * @Project：home-serve
 * @name：CouponServiceImpl
 * @Date：2026/5/26
 * @Filename：CouponServiceImpl
 */
@Slf4j
@Service
public class CouponServiceImpl implements ICouponService {
    @Autowired
    CouponsMapper couponsMapper;

    @Override
    public List<CouponTemplates> getAvailableCoupons() {
        return couponsMapper.getAvailableCoupons();
    }

    @Override
    public void receiveCoupon(Long templateId, Long userId) {
        UserCoupons existing = couponsMapper.getByUserAndTemplate(userId, templateId);
        if (existing != null) {
            throw new BizException("已领取过该优惠券");
        }
        CouponTemplates templates = couponsMapper.getTemplatesById(templateId);
        if (templates == null || templates.getStatus() != 1) {
            throw new BizException("优惠券不存在或已下架");
        }
        UserCoupons userCoupons = UserCoupons.builder()
                .userId(userId)
                .templateId(templateId)
                .status(0)
                .expireTime(LocalDateTime.now().plus(templates.getValidDays(), ChronoUnit.DAYS))
                .createdAt(LocalDateTime.now())
                .build();
        couponsMapper.receiveCoupon(userCoupons);
    }

    @Override
    public List<UserCoupons> getMyCoupons(Long userId, Integer status) {
        return couponsMapper.getCouponsByStatus(status, userId);
    }
}
